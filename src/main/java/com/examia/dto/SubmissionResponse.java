package com.examia.dto;

import com.examia.model.DecisionTreeDefinition;

import com.examia.model.QuestionType;
import com.examia.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Respuesta completa de una entrega, incluyendo los datos de cada pregunta
 * junto con la respuesta del alumno. Usado por la vista de corrección del docente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {

    private String id;
    private String examId;
    private String examTitle;
    private Double totalPoints;

    private StudentInfoDto student;

    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private Double totalScore;
    private String teacherFeedback;
    private LocalDateTime gradedAt;

    private List<AnswerWithQuestionDto> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfoDto {
        private String id;
        private String name;
        private String legajo;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerWithQuestionDto {
        private String questionId;
        private Integer order;
        private String topic;

        // Datos de la pregunta original
        private String questionText;
        private QuestionType questionType;
        private Double points;
        private List<String> options;
        private List<Integer> correctAnswers;
        private String correctTextAnswer;
        private List<String> correctOrder;
        private DecisionTreeDefinition decisionTree;
        private Map<String, String> matchingPairs;
        private List<String> matrixColumnHeaders;
        private List<ArrayList<String>> matrixRows;

        // Respuesta del alumno
        private List<Integer> selectedOptions;
        private String textAnswer;
        private List<String> orderAnswer;
        private Map<String, String> matchingAnswer;
        private DecisionTreeDefinition studentDecisionTree;
        private List<String> studentMatrixColumnHeaders;
        private List<ArrayList<String>> studentMatrixRows;

        // Calificación
        private Double earnedScore;
        private String teacherFeedback;
    }
}
