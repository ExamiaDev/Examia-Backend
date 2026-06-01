package com.examia.service;

import com.examia.dto.GradeSubmissionRequest;
import com.examia.dto.QuestionGradeRequest;
import com.examia.dto.SubmitExamRequest;
import com.examia.dto.StudentAnswerRequest;
import com.examia.model.Exam;
import com.examia.model.Question;
import com.examia.model.QuestionType;
import com.examia.model.Role;
import com.examia.model.StudentAnswer;
import com.examia.model.Submission;
import com.examia.model.SubmissionStatus;
import com.examia.model.User;
import com.examia.repository.ExamRepository;
import com.examia.repository.SubmissionRepository;
import com.examia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private User student;
    private Exam exam;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id("student-123")
                .email("alumno@test.com")
                .nombre("María")
                .apellido("García")
                .legajo("106441")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        Question decisionTree = Question.builder()
                .id("q-tree")
                .type(QuestionType.DECISION_TREE)
                .text("Ordená los pasos del proceso")
                .correctOrder(List.of("Inicio", "Validar", "Guardar", "Finalizar"))
                .points(5.0)
                .required(true)
                .build();

        Question matrix = Question.builder()
                .id("q-matrix")
                .type(QuestionType.MATRIX)
                .text("Relacioná cada término con su significado")
                .matchingPairs(Map.of("Java", "Lenguaje", "Spring", "Framework"))
                .points(5.0)
                .required(true)
                .build();

        exam = Exam.builder()
                .id("exam-123")
                .title("Examen Prueba")
                .professorId("prof-123")
                .subjectId("subject-123")
                .subjectName("Pruebas")
                .questions(List.of(decisionTree, matrix))
                .published(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitExam_savesDecisionTreeAndMatrixAnswers() {
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByExamIdAndStudentIdAndActiveTrue(exam.getId(), student.getId()))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(
                        StudentAnswerRequest.builder()
                                .questionId("q-tree")
                                .orderAnswer(List.of("Inicio", "Validar", "Guardar", "Finalizar"))
                                .build(),
                        StudentAnswerRequest.builder()
                                .questionId("q-matrix")
                                .matchingAnswer(Map.of("Java", "Lenguaje", "Spring", "Framework"))
                                .build()
                ))
                .build();

        var summary = submissionService.submitExam(exam.getId(), request, student);

        assertNotNull(summary);
        assertEquals(exam.getId(), summary.getExamId());
        assertEquals(student.getId(), summary.getStudentId());

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(captor.capture());

        Submission saved = captor.getValue();
        assertNotNull(saved.getAnswers());
        assertEquals(2, saved.getAnswers().size());

        StudentAnswer savedTreeAnswer = saved.getAnswers().stream()
                .filter(answer -> "q-tree".equals(answer.getQuestionId()))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of("Inicio", "Validar", "Guardar", "Finalizar"), savedTreeAnswer.getOrderAnswer());

        StudentAnswer savedMatrixAnswer = saved.getAnswers().stream()
                .filter(answer -> "q-matrix".equals(answer.getQuestionId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Map.of("Java", "Lenguaje", "Spring", "Framework"), savedMatrixAnswer.getMatchingAnswer());
    }

    @Test
    void getSubmissions_returnsSummaryForExam() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-123")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .totalScore(10.0)
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByExamIdAndActiveTrue(exam.getId())).thenReturn(List.of(submission));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        var summaries = submissionService.getSubmissions(exam.getId(), professor);

        assertEquals(1, summaries.size());
        assertEquals("sub-123", summaries.get(0).getId());
        assertEquals("María García", summaries.get(0).getStudentName());
        assertEquals(exam.getTitle(), summaries.get(0).getExamTitle());
    }

    @Test
    void getSubmission_returnsFullResponse() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-456")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .answers(List.of(
                        StudentAnswer.builder().questionId("q-tree").orderAnswer(List.of("Inicio", "Validar")).build(),
                        StudentAnswer.builder().questionId("q-matrix").matchingAnswer(Map.of("Java", "Lenguaje")).build()
                ))
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByIdAndActiveTrue("sub-456")).thenReturn(Optional.of(submission));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        var response = submissionService.getSubmission(exam.getId(), "sub-456", professor);

        assertEquals("sub-456", response.getId());
        assertEquals(2, response.getAnswers().size());
        assertNotNull(response.getStudent());
        assertEquals("María García", response.getStudent().getName());
    }

    @Test
    void gradeSubmission_appliesGradesAndReturnsFullResponse() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-789")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .answers(List.of(StudentAnswer.builder().questionId("q-tree").earnedScore(0.0).build()))
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByIdAndActiveTrue("sub-789")).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        GradeSubmissionRequest request = GradeSubmissionRequest.builder()
                .questionGrades(List.of(QuestionGradeRequest.builder()
                        .questionId("q-tree")
                        .score(5.0)
                        .feedback("Bien hecho")
                        .build()))
                .totalScore(5.0)
                .teacherFeedback("Excelente")
                .build();

        var response = submissionService.gradeSubmission(exam.getId(), "sub-789", request, professor);

        assertEquals(SubmissionStatus.GRADED, response.getStatus());
        assertEquals(5.0, response.getTotalScore());
        assertEquals("Excelente", response.getTeacherFeedback());
        assertEquals(1, response.getAnswers().size());
        assertEquals(5.0, response.getAnswers().get(0).getEarnedScore());
        assertEquals("Bien hecho", response.getAnswers().get(0).getTeacherFeedback());
    }

    @Test
    void getMySubmissions_returnsStudentHistory() {
        Submission submission = Submission.builder()
                .id("sub-321")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        when(submissionRepository.findByStudentIdAndActiveTrue(student.getId())).thenReturn(List.of(submission));
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));

        var history = submissionService.getMySubmissions(student);

        assertEquals(1, history.size());
        assertEquals(exam.getTitle(), history.get(0).getExamTitle());
        assertEquals(student.getId(), history.get(0).getStudentId());
    }

    @Test
    void getMySubmission_returnsFullResponseForStudent() {
        Submission submission = Submission.builder()
                .id("sub-654")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .answers(List.of(StudentAnswer.builder().questionId("q-tree").orderAnswer(List.of("Inicio", "Validar")).build()))
                .build();

        when(submissionRepository.findByIdAndActiveTrue("sub-654")).thenReturn(Optional.of(submission));
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));

        var response = submissionService.getMySubmission("sub-654", student);

        assertEquals("sub-654", response.getId());
        assertEquals(student.getId(), response.getStudent().getId());
        assertEquals(exam.getId(), response.getExamId());
    }
}
