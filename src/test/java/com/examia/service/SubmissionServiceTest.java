package com.examia.service;

import com.examia.dto.GradeSubmissionRequest;
import com.examia.dto.QuestionGradeRequest;
import com.examia.dto.SubmissionSummaryResponse;
import com.examia.dto.SubmitExamRequest;
import com.examia.dto.StudentAnswerRequest;
import com.examia.exception.ExamNotFoundException;
import com.examia.exception.SubmissionNotFoundException;
import com.examia.exception.UnauthorizedAccessException;
import com.examia.model.DecisionTreeDefinition;
import com.examia.model.DecisionTreeNode;
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
import java.time.Month;
import java.util.ArrayList;
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

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, Month.JANUARY, 1, 12, 0, 0);

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
                .createdAt(FIXED_NOW)
                .updatedAt(FIXED_NOW)
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
                .submittedAt(FIXED_NOW)
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
    void getSubmission_includesStudentTreeAndMatrixFields() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();

        DecisionTreeDefinition profTree = DecisionTreeDefinition.builder()
                .nodes(List.of(DecisionTreeNode.builder()
                        .id("n1")
                        .type("decision")
                        .position(Map.of("x", 250.0, "y", 50.0))
                        .data(Map.of("label", "Guía profe"))
                        .build()))
                .build();
        DecisionTreeDefinition studentTree = DecisionTreeDefinition.builder()
                .nodes(List.of(DecisionTreeNode.builder()
                        .id("n1")
                        .type("decision")
                        .position(Map.of("x", 250.0, "y", 50.0))
                        .data(Map.of("label", "Árbol alumno"))
                        .build()))
                .build();

        Question treeQuestion = Question.builder()
                .id("q-tree")
                .type(QuestionType.DECISION_TREE)
                .text("Árbol")
                .order(0)
                .decisionTree(profTree)
                .points(5.0)
                .build();

        Question matrixQuestion = Question.builder()
                .id("q-matrix")
                .type(QuestionType.MATRIX)
                .text("Tabla")
                .order(1)
                .matrixColumnHeaders(List.of("A", "B"))
                .matrixRows(new ArrayList<>(List.of(new ArrayList<>(List.of("1", "2")))))
                .points(5.0)
                .build();

        Exam treeExam = Exam.builder()
                .id("exam-tree")
                .title("Examen árbol/tabla")
                .professorId("prof-123")
                .questions(List.of(treeQuestion, matrixQuestion))
                .published(true)
                .active(true)
                .build();

        Submission submission = Submission.builder()
                .id("sub-tree")
                .examId(treeExam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(FIXED_NOW)
                .answers(List.of(
                        StudentAnswer.builder()
                                .questionId("q-tree")
                                .decisionTree(studentTree)
                                .build(),
                        StudentAnswer.builder()
                                .questionId("q-matrix")
                                .matrixColumnHeaders(List.of("X", "Y"))
                                .matrixRows(new ArrayList<>(List.of(new ArrayList<>(List.of("a", "b")))))
                                .build()
                ))
                .build();

        when(examRepository.findByIdAndActiveTrue(treeExam.getId())).thenReturn(Optional.of(treeExam));
        when(submissionRepository.findByIdAndActiveTrue("sub-tree")).thenReturn(Optional.of(submission));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        var response = submissionService.getSubmission(treeExam.getId(), "sub-tree", professor);

        assertEquals(2, response.getAnswers().size());

        var treeAnswer = response.getAnswers().get(0);
        assertEquals("Guía profe", treeAnswer.getDecisionTree().getNodes().get(0).getData().get("label"));
        assertEquals("Árbol alumno", treeAnswer.getStudentDecisionTree().getNodes().get(0).getData().get("label"));

        var matrixAnswer = response.getAnswers().get(1);
        assertEquals(List.of("A", "B"), matrixAnswer.getMatrixColumnHeaders());
        assertEquals(List.of("X", "Y"), matrixAnswer.getStudentMatrixColumnHeaders());
        assertEquals(List.of(List.of("a", "b")), matrixAnswer.getStudentMatrixRows());
    }

    @Test
    void getSubmission_returnsFullResponse() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-456")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(FIXED_NOW)
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
                .submittedAt(FIXED_NOW)
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
        assertEquals(2, response.getAnswers().size());
        var graded = response.getAnswers().stream()
                .filter(a -> "q-tree".equals(a.getQuestionId()))
                .findFirst()
                .orElseThrow();
        assertEquals(5.0, graded.getEarnedScore());
        assertEquals("Bien hecho", graded.getTeacherFeedback());
    }

    @Test
    void submitExam_whenExamNotFound_throwsExamNotFoundException() {
        when(examRepository.findByIdAndActiveTrue("missing")).thenReturn(Optional.empty());

        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(StudentAnswerRequest.builder().questionId("q-tree").build()))
                .build();

        assertThrows(ExamNotFoundException.class,
                () -> submissionService.submitExam("missing", request, student));
    }

    @Test
    void submitExam_whenNotPublished_throwsUnauthorizedAccessException() {
        exam.setPublished(false);
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));

        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(StudentAnswerRequest.builder().questionId("q-tree").build()))
                .build();

        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.submitExam(exam.getId(), request, student));
    }

    @Test
    void submitExam_whenAlreadySubmitted_throwsIllegalStateException() {
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByExamIdAndStudentIdAndActiveTrue(exam.getId(), student.getId()))
                .thenReturn(Optional.of(Submission.builder().id("existing").build()));

        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(StudentAnswerRequest.builder().questionId("q-tree").build()))
                .build();

        assertThrows(IllegalStateException.class,
                () -> submissionService.submitExam(exam.getId(), request, student));
    }

    @Test
    void submitExam_persistsDecisionTreeAndMatrixFields() {
        DecisionTreeDefinition tree = DecisionTreeDefinition.builder()
                .nodes(List.of(DecisionTreeNode.builder()
                        .id("n1")
                        .type("decision")
                        .position(Map.of("x", 250.0, "y", 50.0))
                        .data(Map.of("label", "Alumno"))
                        .build()))
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByExamIdAndStudentIdAndActiveTrue(exam.getId(), student.getId()))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(
                        StudentAnswerRequest.builder()
                                .questionId("q-tree")
                                .decisionTree(tree)
                                .build(),
                        StudentAnswerRequest.builder()
                                .questionId("q-matrix")
                                .matrixColumnHeaders(List.of("C1", "C2"))
                                .matrixRows(new ArrayList<>(List.of(new ArrayList<>(List.of("a", "b")))))
                                .build()
                ))
                .build();

        submissionService.submitExam(exam.getId(), request, student);

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(captor.capture());
        StudentAnswer treeAnswer = captor.getValue().getAnswers().get(0);
        assertEquals("Alumno", treeAnswer.getDecisionTree().getNodes().get(0).getData().get("label"));
        StudentAnswer matrixAnswer = captor.getValue().getAnswers().get(1);
        assertEquals(List.of("C1", "C2"), matrixAnswer.getMatrixColumnHeaders());
    }

    @Test
    void getSubmissions_whenProfessorNotOwner_throwsUnauthorizedAccessException() {
        User otherProfessor = User.builder().id("other-prof").role(Role.DOCENTE).build();
        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.getSubmissions(exam.getId(), otherProfessor));
    }

    @Test
    void getSubmission_whenSubmissionBelongsToOtherExam_throwsSubmissionNotFoundException() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-456")
                .examId("other-exam")
                .studentId(student.getId())
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByIdAndActiveTrue("sub-456")).thenReturn(Optional.of(submission));

        assertThrows(SubmissionNotFoundException.class,
                () -> submissionService.getSubmission(exam.getId(), "sub-456", professor));
    }

    @Test
    void getMySubmission_whenOtherStudent_throwsUnauthorizedAccessException() {
        Submission submission = Submission.builder()
                .id("sub-654")
                .examId(exam.getId())
                .studentId("other-student")
                .build();

        when(submissionRepository.findByIdAndActiveTrue("sub-654")).thenReturn(Optional.of(submission));

        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.getMySubmission("sub-654", student));
    }

    @Test
    void getSubmissions_whenStudentDeleted_usesFallbackName() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Submission submission = Submission.builder()
                .id("sub-123")
                .examId(exam.getId())
                .studentId("deleted-student")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(FIXED_NOW)
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(exam));
        when(submissionRepository.findByExamIdAndActiveTrue(exam.getId())).thenReturn(List.of(submission));
        when(userRepository.findById("deleted-student")).thenReturn(Optional.empty());

        List<SubmissionSummaryResponse> summaries = submissionService.getSubmissions(exam.getId(), professor);

        assertEquals("Usuario eliminado", summaries.get(0).getStudentName());
        assertNull(summaries.get(0).getStudentLegajo());
    }

    @Test
    void getSubmission_resolvesAnswerByIndexWhenQuestionIdChanged() {
        User professor = User.builder().id("prof-123").role(Role.DOCENTE).build();
        Question q1 = Question.builder().id("new-id-1").type(QuestionType.DECISION_TREE).text("Q1").order(0).points(5.0).build();
        Question q2 = Question.builder().id("new-id-2").type(QuestionType.MATRIX).text("Q2").order(1).points(5.0).build();
        Exam updatedExam = Exam.builder()
                .id(exam.getId())
                .professorId("prof-123")
                .questions(List.of(q1, q2))
                .build();

        Submission submission = Submission.builder()
                .id("sub-index")
                .examId(exam.getId())
                .studentId(student.getId())
                .answers(List.of(
                        StudentAnswer.builder().questionId("old-id-1").textAnswer("resp1").build(),
                        StudentAnswer.builder().questionId("old-id-2").textAnswer("resp2").build()
                ))
                .build();

        when(examRepository.findByIdAndActiveTrue(exam.getId())).thenReturn(Optional.of(updatedExam));
        when(submissionRepository.findByIdAndActiveTrue("sub-index")).thenReturn(Optional.of(submission));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        var response = submissionService.getSubmission(exam.getId(), "sub-index", professor);

        assertEquals("resp1", response.getAnswers().get(0).getTextAnswer());
        assertEquals("resp2", response.getAnswers().get(1).getTextAnswer());
    }

    @Test
    void getMySubmissions_returnsStudentHistory() {
        Submission submission = Submission.builder()
                .id("sub-321")
                .examId(exam.getId())
                .studentId(student.getId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(FIXED_NOW)
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
                .submittedAt(FIXED_NOW)
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
