package com.examia.controller;

import com.examia.dto.*;
import com.examia.model.User;
import com.examia.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de exámenes.
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Gestión de exámenes")
@SecurityRequirement(name = "bearerAuth")
public class ExamController {

    private final ExamService examService;

    /**
     * Crea un nuevo examen.
     * Solo accesible por profesores.
     *
     * POST /api/exams
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Crear un nuevo examen", description = "Crea un nuevo examen con sus preguntas. Solo para profesores.")
    @ApiResponse(responseCode = "201", description = "Examen creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado - Solo profesores")
    public ResponseEntity<ExamResponse> createExam(
            @Valid @RequestBody CreateExamRequest request,
            @AuthenticationPrincipal User professor) {
        ExamResponse response = examService.createExam(request, professor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene todos los exámenes del profesor autenticado.
     * Si se especifica subjectId, filtra por materia.
     *
     * GET /api/exams
     * GET /api/exams?subjectId=xxx
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Obtener exámenes", description = "Obtiene todos los exámenes del profesor, opcionalmente filtrados por materia")
    @ApiResponse(responseCode = "200", description = "Lista de exámenes")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado - Solo profesores")
    public ResponseEntity<List<ExamSummaryResponse>> getExams(
            @Parameter(description = "ID de la materia (opcional)") @RequestParam(required = false) String subjectId,
            @AuthenticationPrincipal User professor) {
        List<ExamSummaryResponse> exams;
        if (subjectId != null && !subjectId.isEmpty()) {
            exams = examService.getExamsByProfessorAndSubject(subjectId, professor);
        } else {
            exams = examService.getExamsByProfessor(professor);
        }
        return ResponseEntity.ok(exams);
    }

    /**
     * Obtiene un examen por su ID.
     *
     * GET /api/exams/{examId}
     */
    @GetMapping("/{examId}")
    @Operation(summary = "Obtener un examen", description = "Obtiene un examen por su ID")
    @ApiResponse(responseCode = "200", description = "Examen encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado para ver este examen")
    @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    public ResponseEntity<ExamResponse> getExam(
            @Parameter(description = "ID del examen") @PathVariable String examId,
            @AuthenticationPrincipal User user) {
        ExamResponse response = examService.getExam(examId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los exámenes del profesor autenticado.
     *
     * GET /api/exams/my-exams
     */
    @GetMapping("/my-exams")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Obtener mis exámenes", description = "Obtiene todos los exámenes creados por el profesor autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de exámenes")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado - Solo profesores")
    public ResponseEntity<List<ExamSummaryResponse>> getMyExams(
            @AuthenticationPrincipal User professor) {
        List<ExamSummaryResponse> exams = examService.getExamsByProfessor(professor);
        return ResponseEntity.ok(exams);
    }

    /**
     * Obtiene todos los exámenes del profesor para una materia específica.
     *
     * GET /api/exams/my-exams/subject/{subjectId}
     */
    @GetMapping("/my-exams/subject/{subjectId}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Obtener mis exámenes por materia", description = "Obtiene los exámenes del profesor para una materia específica")
    @ApiResponse(responseCode = "200", description = "Lista de exámenes")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado - Solo profesores")
    public ResponseEntity<List<ExamSummaryResponse>> getMyExamsBySubject(
            @Parameter(description = "ID de la materia") @PathVariable String subjectId,
            @AuthenticationPrincipal User professor) {
        List<ExamSummaryResponse> exams = examService.getExamsByProfessorAndSubject(subjectId, professor);
        return ResponseEntity.ok(exams);
    }

    /**
     * Obtiene los exámenes publicados de una materia (para alumnos).
     *
     * GET /api/exams/subject/{subjectId}
     */
    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Obtener exámenes publicados de una materia", description = "Obtiene los exámenes publicados y disponibles de una materia")
    @ApiResponse(responseCode = "200", description = "Lista de exámenes")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<List<ExamSummaryResponse>> getPublishedExamsBySubject(
            @Parameter(description = "ID de la materia") @PathVariable String subjectId) {
        List<ExamSummaryResponse> exams = examService.getPublishedExamsBySubject(subjectId);
        return ResponseEntity.ok(exams);
    }

    /**
     * Actualiza un examen existente.
     *
     * PUT /api/exams/{examId}
     */
    @PutMapping("/{examId}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Actualizar un examen", description = "Actualiza un examen existente. Solo para el profesor propietario.")
    @ApiResponse(responseCode = "200", description = "Examen actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar este examen")
    @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    public ResponseEntity<ExamResponse> updateExam(
            @Parameter(description = "ID del examen") @PathVariable String examId,
            @Valid @RequestBody UpdateExamRequest request,
            @AuthenticationPrincipal User professor) {
        ExamResponse response = examService.updateExam(examId, request, professor);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina lógicamente un examen.
     *
     * DELETE /api/exams/{examId}
     */
    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Eliminar un examen", description = "Elimina lógicamente un examen. Solo para el profesor propietario.")
    @ApiResponse(responseCode = "204", description = "Examen eliminado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado para eliminar este examen")
    @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    public ResponseEntity<Void> deleteExam(
            @Parameter(description = "ID del examen") @PathVariable String examId,
            @AuthenticationPrincipal User professor) {
        examService.deleteExam(examId, professor);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publica o despublica un examen.
     *
     * PATCH /api/exams/{examId}/publish
     */
    @PatchMapping("/{examId}/publish")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Publicar/despublicar un examen", description = "Cambia el estado de publicación de un examen")
    @ApiResponse(responseCode = "200", description = "Estado de publicación actualizado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar este examen")
    @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    public ResponseEntity<ExamResponse> togglePublish(
            @Parameter(description = "ID del examen") @PathVariable String examId,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal User professor) {
        boolean published = body.getOrDefault("published", false);
        ExamResponse response = examService.togglePublish(examId, published, professor);
        return ResponseEntity.ok(response);
    }

    /**
     * Duplica un examen existente.
     *
     * POST /api/exams/{examId}/duplicate
     */
    @PostMapping("/{examId}/duplicate")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Duplicar un examen", description = "Crea una copia de un examen existente")
    @ApiResponse(responseCode = "201", description = "Examen duplicado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No autorizado para duplicar este examen")
    @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    public ResponseEntity<ExamResponse> duplicateExam(
            @Parameter(description = "ID del examen a duplicar") @PathVariable String examId,
            @AuthenticationPrincipal User professor) {
        ExamResponse response = examService.duplicateExam(examId, professor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

