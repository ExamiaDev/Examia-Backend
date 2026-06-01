package com.examia.service;

import com.examia.dto.*;
import com.examia.exception.ExamNotFoundException;
import com.examia.exception.SubmissionNotFoundException;
import com.examia.exception.UnauthorizedAccessException;
import com.examia.model.*;
import com.examia.repository.ExamRepository;
import com.examia.repository.SubmissionRepository;
import com.examia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private static final String EXAM_NOT_FOUND = "No se encontró el examen con ID: ";
    private static final String SUBMISSION_NOT_FOUND = "No se encontró la entrega con ID: ";

    private final SubmissionRepository submissionRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    /**
     * Devuelve el listado resumido de entregas para un examen (vista docente).
     */
    public List<SubmissionSummaryResponse> getSubmissions(String examId, User professor) {
        Exam exam = getExamOwnedByProfessor(examId, professor);

        return submissionRepository.findByExamIdAndActiveTrue(examId).stream()
                .map(sub -> {
                    User student = userRepository.findById(sub.getStudentId()).orElse(null);
                    return buildSummary(sub, student, exam);
                })
                .sorted(Comparator.comparing(SubmissionSummaryResponse::getSubmittedAt).reversed())
                .toList();
    }

    /**
     * Devuelve una entrega completa con los datos de cada pregunta para corregir.
     */
    public SubmissionResponse getSubmission(String examId, String submissionId, User professor) {
        Exam exam = getExamOwnedByProfessor(examId, professor);
        Submission submission = getSubmissionBelongingToExam(submissionId, examId);
        User student = userRepository.findById(submission.getStudentId()).orElse(null);
        return buildFullResponse(submission, exam, student);
    }

    /**
     * Califica una entrega: asigna puntajes por pregunta y puntaje total.
     */
    public SubmissionResponse gradeSubmission(String examId, String submissionId,
                                              GradeSubmissionRequest request, User professor) {
        Exam exam = getExamOwnedByProfessor(examId, professor);
        Submission submission = getSubmissionBelongingToExam(submissionId, examId);

        Map<String, QuestionGradeRequest> gradeMap = request.getQuestionGrades().stream()
                .collect(Collectors.toMap(QuestionGradeRequest::getQuestionId, g -> g));

        List<StudentAnswer> updatedAnswers = new ArrayList<>();
        for (StudentAnswer answer : submission.getAnswers()) {
            QuestionGradeRequest grade = gradeMap.get(answer.getQuestionId());
            if (grade != null) {
                answer.setEarnedScore(grade.getScore());
                answer.setTeacherFeedback(grade.getFeedback());
            }
            updatedAnswers.add(answer);
        }

        submission.setAnswers(updatedAnswers);
        submission.setTotalScore(request.getTotalScore());
        submission.setTeacherFeedback(request.getTeacherFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        User student = userRepository.findById(saved.getStudentId()).orElse(null);
        return buildFullResponse(saved, exam, student);
    }

    /**
     * Registra la entrega de un alumno para un examen publicado.
     */
    public SubmissionSummaryResponse submitExam(String examId, SubmitExamRequest request, User student) {
        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(EXAM_NOT_FOUND + examId));

        if (!exam.isPublished()) {
            throw new UnauthorizedAccessException("Este examen no está disponible");
        }

        // Prevenir doble entrega
        Optional<Submission> existing = submissionRepository
                .findByExamIdAndStudentIdAndActiveTrue(examId, student.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Ya realizaste una entrega para este examen");
        }

        List<StudentAnswer> answers = request.getAnswers().stream()
                .map(a -> StudentAnswer.builder()
                        .questionId(a.getQuestionId())
                        .selectedOptions(a.getSelectedOptions())
                        .textAnswer(a.getTextAnswer())
                        .orderAnswer(a.getOrderAnswer())
                        .matchingAnswer(a.getMatchingAnswer())
                        .build())
                .toList();

        Submission submission = Submission.builder()
                .examId(examId)
                .studentId(student.getId())
                .professorId(exam.getProfessorId())
                .subjectId(exam.getSubjectId())
                .answers(answers)
                .status(SubmissionStatus.SUBMITTED)
                .active(true)
                .build();

        Submission saved = submissionRepository.save(submission);
        return buildSummary(saved, student, exam);
    }

    /**
     * Devuelve el historial de entregas del alumno autenticado.
     */
    public List<SubmissionSummaryResponse> getMySubmissions(User student) {
        List<Submission> submissions = submissionRepository.findByStudentIdAndActiveTrue(student.getId());

        return submissions.stream()
                .map(sub -> {
                    Exam exam = examRepository.findByIdAndActiveTrue(sub.getExamId()).orElse(null);
                    return buildSummary(sub, student, exam);
                })
                .sorted(Comparator.comparing(SubmissionSummaryResponse::getSubmittedAt).reversed())
                .toList();
    }

    /**
     * Devuelve el detalle de una entrega del alumno autenticado.
     */
    public SubmissionResponse getMySubmission(String submissionId, User student) {
        Submission submission = submissionRepository.findByIdAndActiveTrue(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(SUBMISSION_NOT_FOUND + submissionId));

        if (!submission.getStudentId().equals(student.getId())) {
            throw new UnauthorizedAccessException("No tiene permiso para ver esta entrega");
        }

        Exam exam = examRepository.findByIdAndActiveTrue(submission.getExamId())
                .orElseThrow(() -> new ExamNotFoundException(EXAM_NOT_FOUND + submission.getExamId()));

        return buildFullResponse(submission, exam, student);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Exam getExamOwnedByProfessor(String examId, User professor) {
        Exam exam = examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ExamNotFoundException(EXAM_NOT_FOUND + examId));
        if (!exam.getProfessorId().equals(professor.getId())) {
            throw new UnauthorizedAccessException("No tiene permiso para acceder a este examen");
        }
        return exam;
    }

    private Submission getSubmissionBelongingToExam(String submissionId, String examId) {
        Submission submission = submissionRepository.findByIdAndActiveTrue(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(SUBMISSION_NOT_FOUND + submissionId));
        if (!submission.getExamId().equals(examId)) {
            throw new SubmissionNotFoundException(SUBMISSION_NOT_FOUND + submissionId);
        }
        return submission;
    }

    private SubmissionSummaryResponse buildSummary(Submission sub, User student, Exam exam) {
        String name = student != null
                ? student.getNombre() + " " + student.getApellido()
                : "Usuario eliminado";
        String legajo = student != null ? student.getLegajo() : null;

        return SubmissionSummaryResponse.builder()
                .id(sub.getId())
                .examId(sub.getExamId())
                .examTitle(exam != null ? exam.getTitle() : null)
                .studentId(sub.getStudentId())
                .studentName(name)
                .studentLegajo(legajo)
                .submittedAt(sub.getSubmittedAt())
                .status(sub.getStatus())
                .totalScore(sub.getTotalScore())
                .totalPoints(exam != null ? exam.getTotalPoints() : null)
                .build();
    }

    private SubmissionResponse buildFullResponse(Submission submission, Exam exam, User student) {
        // Indexar respuestas del alumno por questionId
        Map<String, StudentAnswer> answerMap = submission.getAnswers() == null
                ? Map.of()
                : submission.getAnswers().stream()
                        .collect(Collectors.toMap(StudentAnswer::getQuestionId, a -> a, (a, b) -> a));

        // Ordenar preguntas y combinar con respuestas del alumno
        List<Question> ordered = exam.getQuestions() == null ? List.of() : exam.getQuestions().stream()
                .sorted(Comparator.comparingInt(q -> q.getOrder() != null ? q.getOrder() : 0))
                .toList();

        List<SubmissionResponse.AnswerWithQuestionDto> answers = new ArrayList<>();
        int displayOrder = 1;
        for (Question q : ordered) {
            StudentAnswer sa = answerMap.get(q.getId());
            if (sa == null) continue;

            answers.add(SubmissionResponse.AnswerWithQuestionDto.builder()
                    .questionId(q.getId())
                    .order(displayOrder++)
                    .topic(q.getTopic())
                    .questionText(q.getText())
                    .questionType(q.getType())
                    .points(q.getPoints())
                    .options(q.getOptions())
                    .correctAnswers(q.getCorrectAnswers())
                    .correctTextAnswer(q.getCorrectTextAnswer())
                    .correctOrder(q.getCorrectOrder())
                    .decisionTree(q.getDecisionTree())
                    .matchingPairs(q.getMatchingPairs())
                    .selectedOptions(sa.getSelectedOptions())
                    .textAnswer(sa.getTextAnswer())
                    .orderAnswer(sa.getOrderAnswer())
                    .matchingAnswer(sa.getMatchingAnswer())
                    .earnedScore(sa.getEarnedScore())
                    .teacherFeedback(sa.getTeacherFeedback())
                    .build());
        }

        SubmissionResponse.StudentInfoDto studentDto = null;
        if (student != null) {
            studentDto = SubmissionResponse.StudentInfoDto.builder()
                    .id(student.getId())
                    .name(student.getNombre() + " " + student.getApellido())
                    .legajo(student.getLegajo())
                    .email(student.getEmail())
                    .build();
        }

        return SubmissionResponse.builder()
                .id(submission.getId())
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .totalPoints(exam.getTotalPoints())
                .student(studentDto)
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .totalScore(submission.getTotalScore())
                .teacherFeedback(submission.getTeacherFeedback())
                .gradedAt(submission.getGradedAt())
                .answers(answers)
                .build();
    }
}
