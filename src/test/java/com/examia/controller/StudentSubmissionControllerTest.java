package com.examia.controller;

import com.examia.dto.SubmissionResponse;
import com.examia.dto.SubmissionSummaryResponse;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentSubmissionControllerTest {

    private final SubmissionService submissionService = mock(SubmissionService.class);
    private final StudentSubmissionController controller = new StudentSubmissionController(submissionService);

    private final User student = User.builder().id("stu-1").role(Role.ALUMNO).build();

    @Test
    void getMySubmissions_returnsOk() {
        SubmissionSummaryResponse summary = SubmissionSummaryResponse.builder().id("sub-1").build();
        when(submissionService.getMySubmissions(student)).thenReturn(List.of(summary));

        ResponseEntity<List<SubmissionSummaryResponse>> response = controller.getMySubmissions(student);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMySubmission_returnsOk() {
        SubmissionResponse full = SubmissionResponse.builder().id("sub-1").build();
        when(submissionService.getMySubmission("sub-1", student)).thenReturn(full);

        ResponseEntity<SubmissionResponse> response = controller.getMySubmission("sub-1", student);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sub-1", response.getBody().getId());
    }
}
