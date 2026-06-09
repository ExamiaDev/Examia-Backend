package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringViolation {
    private String type;      // tab_switch | window_blur
    private String timestamp; // ISO-8601
}
