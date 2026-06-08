package com.examia.dto;

import com.examia.model.ProctoringViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExamRequest {

    @NotEmpty(message = "Debe incluir al menos una respuesta")
    @Valid
    private List<StudentAnswerRequest> answers;

    @Builder.Default
    private List<ProctoringViolation> violations = new ArrayList<>();

    private Integer timeTakenSeconds;
}
