package com.examia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerRequest {

    @NotBlank(message = "El ID de la pregunta es obligatorio")
    private String questionId;

    /** Para MULTIPLE_CHOICE, MULTIPLE_SELECTION, TRUE_FALSE */
    private List<Integer> selectedOptions;

    /** Para LONG_ANSWER, SHORT_ANSWER, FILL_IN_THE_BLANK */
    private String textAnswer;

    /** Para ORDERING */
    private List<String> orderAnswer;

    /** Para MATCHING */
    private Map<String, String> matchingAnswer;
}
