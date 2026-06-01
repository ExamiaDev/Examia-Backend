package com.examia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExamRequest {

    @NotEmpty(message = "Debe incluir al menos una respuesta")
    @Valid
    private List<StudentAnswerRequest> answers;
}
