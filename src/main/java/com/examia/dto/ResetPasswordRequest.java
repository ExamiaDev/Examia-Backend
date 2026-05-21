package com.examia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    private String code;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;
}
