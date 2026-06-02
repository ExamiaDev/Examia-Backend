package com.examia.service;

import com.examia.dto.*;
import com.examia.exception.ExamNotFoundException;
import com.examia.exception.UnauthorizedAccessException;
import com.examia.model.Exam;
import com.examia.model.Question;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de exámenes.
 */
@Service
@RequiredArgsConstructor
public class ExamService {

    private static final String EXAM_NOT_FOUND_MESSAGE = "No se encontró el examen con ID: ";
    private final ExamRepository examRepository;

    /**
     * Crea un nuevo examen.
     *
     * @param request datos del examen a crear
     * @param professor el usuario profesor que crea el examen
     * @return ExamResponse con los datos del examen creado
     */
    public ExamResponse createExam(CreateExamRequest request, User professor) {
        validateProfessorRole(professor);

        List<Question> questions = mapQuestionsFromRequest(request.getQuestions());
        double totalPoints = calculateTotalPoints(questions);

        Exam exam = Exam.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .professorId(professor.getId())
                .subjectId(request.getSubjectId())
                .subjectName(request.getSubjectName())
                .questions(questions)
                .durationMinutes(request.getDurationMinutes())
                .totalPoints(totalPoints)
                .passingScore(request.getPassingScore())
                .scheduledStartTime(request.getScheduledStartTime())
                .scheduledEndTime(request.getScheduledEndTime())
                .published(request.isPublished())
                .shuffleQuestions(request.isShuffleQuestions())
                .shuffleOptions(request.isShuffleOptions())
                .showResultsOnCompletion(request.isShowResultsOnCompletion())
                .maxAttempts(request.getMaxAttempts())
                .shift(request.getShift())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Exam savedExam = examRepository.save(exam);
        return buildExamResponse(savedExam, "Examen creado exitosamente");
    }

    /**
     * Obtiene un examen por su ID.
     *
     * @param examId ID del examen
     * @param user el usuario que solicita el examen
     * @return ExamResponse con los datos del examen
     */
    public ExamResponse getExam(String examId, User user) {
        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        EXAM_NOT_FOUND_MESSAGE + examId));

        // Si es profesor, solo puede ver sus propios exámenes
        // Si es alumno, solo puede ver exámenes publicados
        if (user.getRole() == Role.DOCENTE) {
            if (!exam.getProfessorId().equals(user.getId())) {
                throw new UnauthorizedAccessException(
                        "No tiene permiso para ver este examen");
            }
        } else {
            if (!exam.isPublished()) {
                throw new UnauthorizedAccessException(
                        "Este examen no está disponible");
            }
        }

        return buildExamResponse(exam, null);
    }

    /**
     * Obtiene todos los exámenes de un profesor.
     *
     * @param professor el profesor
     * @return lista de ExamSummaryResponse
     */
    public List<ExamSummaryResponse> getExamsByProfessor(User professor) {
        validateProfessorRole(professor);

        List<Exam> exams = examRepository.findByProfessorIdAndActiveTrue(professor.getId());
        return exams.stream()
                .map(this::buildExamSummaryResponse)
                .toList();
    }

    /**
     * Obtiene todos los exámenes de un profesor para una materia específica.
     *
     * @param subjectId ID de la materia
     * @param professor el profesor
     * @return lista de ExamSummaryResponse
     */
    public List<ExamSummaryResponse> getExamsByProfessorAndSubject(String subjectId, User professor) {
        validateProfessorRole(professor);

        List<Exam> exams = examRepository.findByProfessorIdAndSubjectIdAndActiveTrue(
                professor.getId(), subjectId);
        return exams.stream()
                .map(this::buildExamSummaryResponse)
                .toList();
    }

    /**
     * Obtiene todos los exámenes publicados de una materia (para alumnos).
     *
     * @param subjectId ID de la materia
     * @return lista de ExamSummaryResponse
     */
    public List<ExamSummaryResponse> getPublishedExamsBySubject(String subjectId) {
        List<Exam> exams = examRepository.findBySubjectIdAndPublishedTrueAndActiveTrue(subjectId);
        return exams.stream()
                .map(this::buildExamSummaryResponse)
                .toList();
    }

    /**
     * Obtiene todos los exámenes publicados y activos (para alumnos, sin filtro de materia).
     *
     * @return lista de ExamSummaryResponse
     */
    public List<ExamSummaryResponse> getAllPublishedExams() {
        List<Exam> exams = examRepository.findByPublishedTrueAndActiveTrue();
        return exams.stream()
                .map(this::buildExamSummaryResponse)
                .toList();
    }

    /**
     * Actualiza un examen existente.
     *
     * @param examId ID del examen a actualizar
     * @param request datos actualizados
     * @param professor el profesor propietario
     * @return ExamResponse con los datos actualizados
     */
    public ExamResponse updateExam(String examId, UpdateExamRequest request, User professor) {
        validateProfessorRole(professor);

        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        EXAM_NOT_FOUND_MESSAGE + examId));

        if (!exam.getProfessorId().equals(professor.getId())) {
            throw new UnauthorizedAccessException(
                    "No tiene permiso para modificar este examen");
        }

        // Actualizar campos
        exam.setTitle(request.getTitle());

        if (request.getDescription() != null) {
            exam.setDescription(request.getDescription());
        }
        if (request.getSubjectId() != null) {
            exam.setSubjectId(request.getSubjectId());
        }
        if (request.getSubjectName() != null) {
            exam.setSubjectName(request.getSubjectName());
        }
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = mapQuestionsFromRequest(request.getQuestions());
            exam.setQuestions(questions);
            exam.setTotalPoints(calculateTotalPoints(questions));
        }
        if (request.getDurationMinutes() != null) {
            exam.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getPassingScore() != null) {
            exam.setPassingScore(request.getPassingScore());
        }
        if (request.getScheduledStartTime() != null) {
            exam.setScheduledStartTime(request.getScheduledStartTime());
        }
        if (request.getScheduledEndTime() != null) {
            exam.setScheduledEndTime(request.getScheduledEndTime());
        }
        if (request.getPublished() != null) {
            exam.setPublished(request.getPublished());
        }
        if (request.getShuffleQuestions() != null) {
            exam.setShuffleQuestions(request.getShuffleQuestions());
        }
        if (request.getShuffleOptions() != null) {
            exam.setShuffleOptions(request.getShuffleOptions());
        }
        if (request.getShowResultsOnCompletion() != null) {
            exam.setShowResultsOnCompletion(request.getShowResultsOnCompletion());
        }
        if (request.getMaxAttempts() != null) {
            exam.setMaxAttempts(request.getMaxAttempts());
        }
        if (request.getShift() != null) {
            exam.setShift(request.getShift());
        }

        exam.setUpdatedAt(LocalDateTime.now());

        Exam updatedExam = examRepository.save(exam);
        return buildExamResponse(updatedExam, "Examen actualizado exitosamente");
    }

    /**
     * Elimina lógicamente un examen (soft delete).
     *
     * @param examId ID del examen a eliminar
     * @param professor el profesor propietario
     */
    public void deleteExam(String examId, User professor) {
        validateProfessorRole(professor);

        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        EXAM_NOT_FOUND_MESSAGE + examId));

        if (!exam.getProfessorId().equals(professor.getId())) {
            throw new UnauthorizedAccessException(
                    "No tiene permiso para eliminar este examen");
        }

        exam.setActive(false);
        exam.setDeletedAt(LocalDateTime.now());
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    /**
     * Publica o despublica un examen.
     *
     * @param examId ID del examen
     * @param published nuevo estado de publicación
     * @param professor el profesor propietario
     * @return ExamResponse con los datos actualizados
     */
    public ExamResponse togglePublish(String examId, boolean published, User professor) {
        validateProfessorRole(professor);

        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        EXAM_NOT_FOUND_MESSAGE + examId));

        if (!exam.getProfessorId().equals(professor.getId())) {
            throw new UnauthorizedAccessException(
                    "No tiene permiso para modificar este examen");
        }

        exam.setPublished(published);
        exam.setUpdatedAt(LocalDateTime.now());

        Exam updatedExam = examRepository.save(exam);
        String message = published ? "Examen publicado exitosamente" : "Examen despublicado exitosamente";
        return buildExamResponse(updatedExam, message);
    }

    /**
     * Duplica un examen existente.
     *
     * @param examId ID del examen a duplicar
     * @param professor el profesor propietario
     * @return ExamResponse con los datos del nuevo examen
     */
    public ExamResponse duplicateExam(String examId, User professor) {
        validateProfessorRole(professor);

        Exam originalExam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        EXAM_NOT_FOUND_MESSAGE + examId));

        if (!originalExam.getProfessorId().equals(professor.getId())) {
            throw new UnauthorizedAccessException(
                    "No tiene permiso para duplicar este examen");
        }

        // Crear copia con nuevos IDs para las preguntas
        List<Question> duplicatedQuestions = originalExam.getQuestions().stream()
                .map(q -> Question.builder()
                        .id(UUID.randomUUID().toString())
                        .type(q.getType())
                        .text(q.getText())
                        .options(q.getOptions())
                        .correctAnswers(q.getCorrectAnswers())
                        .correctTextAnswer(q.getCorrectTextAnswer())
                        .matchingPairs(q.getMatchingPairs())
                        .matrixColumnHeaders(q.getMatrixColumnHeaders())
                        .matrixRows(q.getMatrixRows())
                        .correctOrder(q.getCorrectOrder())
                        .decisionTree(q.getDecisionTree())
                        .points(q.getPoints())
                        .explanation(q.getExplanation())
                        .imageUrl(q.getImageUrl())
                        .topic(q.getTopic())
                        .topicColor(q.getTopicColor())
                        .order(q.getOrder())
                        .required(q.isRequired())
                        .build())
                .toList();

        Exam duplicatedExam = Exam.builder()
                .title(originalExam.getTitle() + " (Copia)")
                .description(originalExam.getDescription())
                .professorId(professor.getId())
                .subjectId(originalExam.getSubjectId())
                .subjectName(originalExam.getSubjectName())
                .questions(duplicatedQuestions)
                .durationMinutes(originalExam.getDurationMinutes())
                .totalPoints(originalExam.getTotalPoints())
                .passingScore(originalExam.getPassingScore())
                .shuffleQuestions(originalExam.isShuffleQuestions())
                .shuffleOptions(originalExam.isShuffleOptions())
                .showResultsOnCompletion(originalExam.isShowResultsOnCompletion())
                .maxAttempts(originalExam.getMaxAttempts())
                .published(false) // Nueva copia no publicada
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Exam savedExam = examRepository.save(duplicatedExam);
        return buildExamResponse(savedExam, "Examen duplicado exitosamente");
    }

    // ============== Métodos auxiliares ==============

    private void validateProfessorRole(User user) {
        if (user.getRole() != Role.DOCENTE) {
            throw new UnauthorizedAccessException(
                    "Solo los profesores pueden realizar esta acción");
        }
    }

    private List<Question> mapQuestionsFromRequest(List<QuestionRequest> questionRequests) {
        return questionRequests.stream()
                .map(this::mapQuestionFromRequest)
                .toList();
    }

    private Question mapQuestionFromRequest(QuestionRequest request) {
        return Question.builder()
                .id(request.getId() != null ? request.getId() : UUID.randomUUID().toString())
                .type(request.getType())
                .text(request.getText())
                .options(request.getOptions())
                .correctAnswers(request.getCorrectAnswers())
                .correctTextAnswer(request.getCorrectTextAnswer())
                .matchingPairs(request.getMatchingPairs())
                .matrixColumnHeaders(request.getMatrixColumnHeaders())
                .matrixRows(request.getMatrixRows())
                .correctOrder(request.getCorrectOrder())
                .decisionTree(request.getDecisionTree())
                .points(request.getPoints() != null ? request.getPoints() : 1.0)
                .explanation(request.getExplanation())
                .imageUrl(request.getImageUrl())
                .topic(request.getTopic())
                .topicColor(request.getTopicColor())
                .order(request.getOrder())
                .required(request.isRequired())
                .build();
    }

    private double calculateTotalPoints(List<Question> questions) {
        return questions.stream()
                .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 1.0)
                .sum();
    }

    private ExamResponse buildExamResponse(Exam exam, String message) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .professorId(exam.getProfessorId())
                .subjectId(exam.getSubjectId())
                .subjectName(exam.getSubjectName())
                .questions(exam.getQuestions())
                .durationMinutes(exam.getDurationMinutes())
                .totalPoints(exam.getTotalPoints())
                .passingScore(exam.getPassingScore())
                .scheduledStartTime(exam.getScheduledStartTime())
                .scheduledEndTime(exam.getScheduledEndTime())
                .shift(exam.getShift())
                .published(exam.isPublished())
                .shuffleQuestions(exam.isShuffleQuestions())
                .shuffleOptions(exam.isShuffleOptions())
                .showResultsOnCompletion(exam.isShowResultsOnCompletion())
                .maxAttempts(exam.getMaxAttempts())
                .questionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0)
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .message(message)
                .build();
    }

    private ExamSummaryResponse buildExamSummaryResponse(Exam exam) {
        return ExamSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .professorId(exam.getProfessorId())
                .subjectId(exam.getSubjectId())
                .subjectName(exam.getSubjectName())
                .questionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0)
                .durationMinutes(exam.getDurationMinutes())
                .totalPoints(exam.getTotalPoints())
                .passingScore(exam.getPassingScore())
                .scheduledStartTime(exam.getScheduledStartTime())
                .scheduledEndTime(exam.getScheduledEndTime())
                .shift(exam.getShift())
                .published(exam.isPublished())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }
}

