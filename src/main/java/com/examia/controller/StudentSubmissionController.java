package com.examia.controller;

import com.examia.dto.SubmissionResponse;
import com.examia.dto.SubmissionSummaryResponse;
import com.examia.model.User;
import com.examia.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class StudentSubmissionController {

    private final SubmissionService submissionService;

    /**
     * Alumno: historial de sus propias entregas.
     * GET /api/submissions/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<SubmissionSummaryResponse>> getMySubmissions(
            @AuthenticationPrincipal User student) {

        List<SubmissionSummaryResponse> submissions = submissionService.getMySubmissions(student);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Alumno: detalle de una de sus entregas (incluye corrección si ya fue calificada).
     * GET /api/submissions/{submissionId}
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<SubmissionResponse> getMySubmission(
            @PathVariable String submissionId,
            @AuthenticationPrincipal User student) {

        SubmissionResponse response = submissionService.getMySubmission(submissionId, student);
        return ResponseEntity.ok(response);
    }
}
