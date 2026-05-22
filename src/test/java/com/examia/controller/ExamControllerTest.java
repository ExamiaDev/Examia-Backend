package com.examia.controller;

import com.examia.dto.*;
import com.examia.model.Question;
import com.examia.model.QuestionType;
import com.examia.service.ExamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ExamController.
 */
@ExtendWith(MockitoExtension.class)
class ExamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExamService examService;

    @InjectMocks
    private ExamController examController;

    private ObjectMapper objectMapper;
    private CreateExamRequest createExamRequest;
    private ExamResponse examResponse;
    private ExamSummaryResponse examSummaryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(examController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


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

        Question question = Question.builder()
                .id("q-123")
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("¿Cuál es la capital de Argentina?")
                .options(Arrays.asList("Buenos Aires", "Córdoba", "Rosario", "Mendoza"))
                .correctAnswers(Collections.singletonList(0))
                .points(10.0)
                .build();

        examResponse = ExamResponse.builder()
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
                .questionCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .message("Examen creado exitosamente")
                .build();

        examSummaryResponse = ExamSummaryResponse.builder()
                .id("exam-123")
                .title("Examen de Geografía")
                .description("Examen sobre capitales de países")
                .professorId("prof-123")
                .subjectId("subject-123")
                .subjectName("Geografía")
                .questionCount(1)
                .durationMinutes(60)
                .totalPoints(10.0)
                .passingScore(60.0)
                .published(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createExam_withValidRequest_returnsCreated() throws Exception {
        when(examService.createExam(any(CreateExamRequest.class), any(User.class)))
                .thenReturn(examResponse);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExamRequest))
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isCreated());
    }

    @Test
    void getMyExams_returnsListOfExams() throws Exception {
        List<ExamSummaryResponse> exams = Collections.singletonList(examSummaryResponse);
        when(examService.getExamsByProfessor(any(User.class))).thenReturn(exams);

        mockMvc.perform(get("/api/exams/my-exams")
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyExamsBySubject_returnsListOfExams() throws Exception {
        List<ExamSummaryResponse> exams = Collections.singletonList(examSummaryResponse);
        when(examService.getExamsByProfessorAndSubject(anyString(), any(User.class))).thenReturn(exams);

        mockMvc.perform(get("/api/exams/my-exams/subject/subject-123")
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublishedExamsBySubject_returnsListOfExams() throws Exception {
        List<ExamSummaryResponse> exams = Collections.singletonList(examSummaryResponse);
        when(examService.getPublishedExamsBySubject(anyString())).thenReturn(exams);

        mockMvc.perform(get("/api/exams/subject/subject-123"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteExam_returnsNoContent() throws Exception {
        doNothing().when(examService).deleteExam(anyString(), any(User.class));

        mockMvc.perform(delete("/api/exams/exam-123")
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void togglePublish_returnsUpdatedExam() throws Exception {
        examResponse.setPublished(true);
        examResponse.setMessage("Examen publicado exitosamente");
        when(examService.togglePublish(anyString(), anyBoolean(), any(User.class)))
                .thenReturn(examResponse);

        mockMvc.perform(patch("/api/exams/exam-123/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"published\": true}")
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateExam_returnsCreated() throws Exception {
        examResponse.setTitle("Examen de Geografía (Copia)");
        examResponse.setMessage("Examen duplicado exitosamente");
        when(examService.duplicateExam(anyString(), any(User.class)))
                .thenReturn(examResponse);

        mockMvc.perform(post("/api/exams/exam-123/duplicate")
                        .principal(() -> "profesor@test.com"))
                .andExpect(status().isCreated());
    }
}

