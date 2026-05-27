package com.examia.controller;

import com.examia.dto.*;
import com.examia.model.User;
import com.examia.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams/{examId}/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Docente: listado de entregas de un examen.
     * GET /api/exams/{examId}/submissions
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<SubmissionSummaryResponse>> getSubmissions(
            @PathVariable String examId,
            @AuthenticationPrincipal User professor) {

        List<SubmissionSummaryResponse> submissions = submissionService.getSubmissions(examId, professor);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Docente: entrega completa con preguntas y respuestas del alumno para corregir.
     * GET /api/exams/{examId}/submissions/{submissionId}
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @PathVariable String examId,
            @PathVariable String submissionId,
            @AuthenticationPrincipal User professor) {

        SubmissionResponse response = submissionService.getSubmission(examId, submissionId, professor);
        return ResponseEntity.ok(response);
    }

    /**
     * Docente: calificar una entrega.
     * POST /api/exams/{examId}/submissions/{submissionId}/grade
     */
    @PostMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<SubmissionResponse> gradeSubmission(
            @PathVariable String examId,
            @PathVariable String submissionId,
            @Valid @RequestBody GradeSubmissionRequest request,
            @AuthenticationPrincipal User professor) {

        SubmissionResponse response = submissionService.gradeSubmission(examId, submissionId, request, professor);
        return ResponseEntity.ok(response);
    }

    /**
     * Alumno: entregar respuestas de un examen.
     * POST /api/exams/{examId}/submissions
     */
    @PostMapping
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<SubmissionSummaryResponse> submitExam(
            @PathVariable String examId,
            @Valid @RequestBody SubmitExamRequest request,
            @AuthenticationPrincipal User student) {

        SubmissionSummaryResponse response = submissionService.submitExam(examId, request, student);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
