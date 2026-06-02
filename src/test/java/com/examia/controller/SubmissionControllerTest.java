package com.examia.controller;

import com.examia.dto.GradeSubmissionRequest;
import com.examia.dto.SubmissionResponse;
import com.examia.dto.SubmissionSummaryResponse;
import com.examia.dto.SubmitExamRequest;
import com.examia.dto.StudentAnswerRequest;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SubmissionControllerTest {

    private final SubmissionService submissionService = mock(SubmissionService.class);
    private final SubmissionController controller = new SubmissionController(submissionService);

    private final User professor = User.builder().id("prof-1").role(Role.DOCENTE).build();
    private final User student = User.builder().id("stu-1").role(Role.ALUMNO).build();

    @Test
    void getSubmissions_returnsOk() {
        SubmissionSummaryResponse summary = SubmissionSummaryResponse.builder().id("sub-1").build();
        when(submissionService.getSubmissions("exam-1", professor)).thenReturn(List.of(summary));

        ResponseEntity<List<SubmissionSummaryResponse>> response =
                controller.getSubmissions("exam-1", professor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSubmission_returnsOk() {
        SubmissionResponse full = SubmissionResponse.builder().id("sub-1").build();
        when(submissionService.getSubmission("exam-1", "sub-1", professor)).thenReturn(full);

        ResponseEntity<SubmissionResponse> response =
                controller.getSubmission("exam-1", "sub-1", professor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sub-1", response.getBody().getId());
    }

    @Test
    void gradeSubmission_returnsOk() {
        SubmissionResponse graded = SubmissionResponse.builder().id("sub-1").totalScore(10.0).build();
        GradeSubmissionRequest request = GradeSubmissionRequest.builder().totalScore(10.0).build();
        when(submissionService.gradeSubmission(eq("exam-1"), eq("sub-1"), any(), eq(professor)))
                .thenReturn(graded);

        ResponseEntity<SubmissionResponse> response =
                controller.gradeSubmission("exam-1", "sub-1", request, professor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.0, response.getBody().getTotalScore());
    }

    @Test
    void submitExam_returnsCreated() {
        SubmissionSummaryResponse summary = SubmissionSummaryResponse.builder().id("sub-new").build();
        SubmitExamRequest request = SubmitExamRequest.builder()
                .answers(List.of(StudentAnswerRequest.builder().questionId("q1").build()))
                .build();
        when(submissionService.submitExam("exam-1", request, student)).thenReturn(summary);

        ResponseEntity<SubmissionSummaryResponse> response =
                controller.submitExam("exam-1", request, student);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("sub-new", response.getBody().getId());
    }
}
