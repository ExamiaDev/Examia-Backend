package com.examia.service;

import com.examia.dto.*;
import com.examia.exception.ExamNotFoundException;
import com.examia.exception.UnauthorizedAccessException;
import com.examia.model.*;
import com.examia.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for ExamService.
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ExamService examService;

    private User professor;
    private User student;
    private Exam exam;
    private CreateExamRequest createExamRequest;
    private UpdateExamRequest updateExamRequest;

    @BeforeEach
    void setUp() {
        professor = User.builder()
                .id("prof-123")
                .email("profesor@test.com")
                .nombre("Juan")
                .apellido("Pérez")
                .role(Role.DOCENTE)
                .enabled(true)
                .build();

        student = User.builder()
                .id("student-123")
                .email("alumno@test.com")
                .nombre("María")
                .apellido("García")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        Question question = Question.builder()
                .id("q-123")
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("¿Cuál es la capital de Argentina?")
                .options(Arrays.asList("Buenos Aires", "Córdoba", "Rosario", "Mendoza"))
                .correctAnswers(Collections.singletonList(0))
                .points(10.0)
                .required(true)
                .build();

        exam = Exam.builder()
                .id("exam-123")
                .title("Examen de Geografía")
                .description("Examen sobre capitales de países")
                .professorId("prof-123")
                .subjectId("subject-123")
                .subjectName("Geografía")
                .questions(Collections.singletonList(question))
                .durationMinutes(60)
                .totalPoints(10.0)
                .passingScore(60.0)
                .published(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        QuestionRequest questionRequest = QuestionRequest.builder()
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("¿Cuál es la capital de Argentina?")
                .options(Arrays.asList("Buenos Aires", "Córdoba", "Rosario", "Mendoza"))
                .correctAnswers(Collections.singletonList(0))
                .points(10.0)
                .build();

        createExamRequest = CreateExamRequest.builder()
                .title("Examen de Geografía")
                .description("Examen sobre capitales de países")
                .subjectId("subject-123")
                .subjectName("Geografía")
                .questions(Collections.singletonList(questionRequest))
                .durationMinutes(60)
                .passingScore(60.0)
                .published(false)
                .build();

        updateExamRequest = UpdateExamRequest.builder()
                .title("Examen de Geografía Actualizado")
                .description("Nueva descripción")
                .build();
    }

    // ============== Create Exam Tests ==============

    @Test
    void createExam_withValidRequest_returnsExamResponse() {
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> {
            Exam savedExam = invocation.getArgument(0);
            savedExam.setId("exam-123");
            return savedExam;
        });

        ExamResponse response = examService.createExam(createExamRequest, professor);

        assertNotNull(response);
        assertEquals("Examen de Geografía", response.getTitle());
        assertEquals("Examen creado exitosamente", response.getMessage());
        verify(examRepository).save(any(Exam.class));
    }

    @Test
    void createExam_asStudent_throwsUnauthorizedAccessException() {
        assertThrows(UnauthorizedAccessException.class,
                () -> examService.createExam(createExamRequest, student));

        verify(examRepository, never()).save(any(Exam.class));
    }

    // ============== Get Exam Tests ==============

    @Test
    void getExam_asProfessorOwner_returnsExamResponse() {
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        ExamResponse response = examService.getExam("exam-123", professor);

        assertNotNull(response);
        assertEquals("exam-123", response.getId());
        assertEquals("Examen de Geografía", response.getTitle());
    }

    @Test
    void getExam_asProfessorNotOwner_throwsUnauthorizedAccessException() {
        User otherProfessor = User.builder()
                .id("other-prof-123")
                .role(Role.DOCENTE)
                .build();
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> examService.getExam("exam-123", otherProfessor));
    }

    @Test
    void getExam_asStudentPublishedExam_returnsExamResponse() {
        exam.setPublished(true);
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        ExamResponse response = examService.getExam("exam-123", student);

        assertNotNull(response);
        assertEquals("exam-123", response.getId());
    }

    @Test
    void getExam_asStudentUnpublishedExam_throwsUnauthorizedAccessException() {
        exam.setPublished(false);
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> examService.getExam("exam-123", student));
    }

    @Test
    void getExam_nonExistent_throwsExamNotFoundException() {
        when(examRepository.findByIdAndActiveTrue("non-existent")).thenReturn(Optional.empty());

        assertThrows(ExamNotFoundException.class,
                () -> examService.getExam("non-existent", professor));
    }

    // ============== Get Exams by Professor Tests ==============

    @Test
    void getExamsByProfessor_returnsList() {
        when(examRepository.findByProfessorIdAndActiveTrue("prof-123"))
                .thenReturn(Collections.singletonList(exam));

        List<ExamSummaryResponse> response = examService.getExamsByProfessor(professor);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("exam-123", response.get(0).getId());
    }

    @Test
    void getExamsByProfessor_asStudent_throwsUnauthorizedAccessException() {
        assertThrows(UnauthorizedAccessException.class,
                () -> examService.getExamsByProfessor(student));
    }

    // ============== Get Exams by Professor and Subject Tests ==============

    @Test
    void getExamsByProfessorAndSubject_returnsList() {
        when(examRepository.findByProfessorIdAndSubjectIdAndActiveTrue("prof-123", "subject-123"))
                .thenReturn(Collections.singletonList(exam));

        List<ExamSummaryResponse> response = examService.getExamsByProfessorAndSubject("subject-123", professor);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    // ============== Get Published Exams by Subject Tests ==============

    @Test
    void getPublishedExamsBySubject_returnsList() {
        exam.setPublished(true);
        when(examRepository.findBySubjectIdAndPublishedTrueAndActiveTrue("subject-123"))
                .thenReturn(Collections.singletonList(exam));

        List<ExamSummaryResponse> response = examService.getPublishedExamsBySubject("subject-123");

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    // ============== Update Exam Tests ==============

    @Test
    void updateExam_asProfessorOwner_returnsUpdatedExam() {
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExamResponse response = examService.updateExam("exam-123", updateExamRequest, professor);

        assertNotNull(response);
        assertEquals("Examen de Geografía Actualizado", response.getTitle());
        assertEquals("Examen actualizado exitosamente", response.getMessage());
    }

    @Test
    void updateExam_asProfessorNotOwner_throwsUnauthorizedAccessException() {
        User otherProfessor = User.builder()
                .id("other-prof-123")
                .role(Role.DOCENTE)
                .build();
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> examService.updateExam("exam-123", updateExamRequest, otherProfessor));
    }

    @Test
    void updateExam_nonExistent_throwsExamNotFoundException() {
        when(examRepository.findByIdAndActiveTrue("non-existent")).thenReturn(Optional.empty());

        assertThrows(ExamNotFoundException.class,
                () -> examService.updateExam("non-existent", updateExamRequest, professor));
    }

    // ============== Delete Exam Tests ==============

    @Test
    void deleteExam_asProfessorOwner_setsActiveToFalse() {
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examService.deleteExam("exam-123", professor);

        verify(examRepository).save(argThat(savedExam ->
                !savedExam.isActive() && savedExam.getDeletedAt() != null));
    }

    @Test
    void deleteExam_asProfessorNotOwner_throwsUnauthorizedAccessException() {
        User otherProfessor = User.builder()
                .id("other-prof-123")
                .role(Role.DOCENTE)
                .build();
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> examService.deleteExam("exam-123", otherProfessor));
    }

    @Test
    void deleteExam_asStudent_throwsUnauthorizedAccessException() {
        assertThrows(UnauthorizedAccessException.class,
                () -> examService.deleteExam("exam-123", student));
    }

    // ============== Toggle Publish Tests ==============

    @Test
    void togglePublish_toPublished_returnsUpdatedExam() {
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExamResponse response = examService.togglePublish("exam-123", true, professor);

        assertNotNull(response);
        assertTrue(response.isPublished());
        assertEquals("Examen publicado exitosamente", response.getMessage());
    }

    @Test
    void togglePublish_toUnpublished_returnsUpdatedExam() {
        exam.setPublished(true);
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExamResponse response = examService.togglePublish("exam-123", false, professor);

        assertNotNull(response);
        assertFalse(response.isPublished());
        assertEquals("Examen despublicado exitosamente", response.getMessage());
    }

    // ============== Duplicate Exam Tests ==============

    @Test
    void duplicateExam_asProfessorOwner_returnsNewExam() {
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> {
            Exam savedExam = invocation.getArgument(0);
            savedExam.setId("exam-456");
            return savedExam;
        });

        ExamResponse response = examService.duplicateExam("exam-123", professor);

        assertNotNull(response);
        assertEquals("Examen de Geografía (Copia)", response.getTitle());
        assertFalse(response.isPublished());
        assertEquals("Examen duplicado exitosamente", response.getMessage());
    }

    @Test
    void duplicateExam_asProfessorNotOwner_throwsUnauthorizedAccessException() {
        User otherProfessor = User.builder()
                .id("other-prof-123")
                .role(Role.DOCENTE)
                .build();
        when(examRepository.findByIdAndActiveTrue("exam-123")).thenReturn(Optional.of(exam));

        assertThrows(UnauthorizedAccessException.class,
                () -> examService.duplicateExam("exam-123", otherProfessor));
    }
}

