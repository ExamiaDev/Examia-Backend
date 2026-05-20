package com.examia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de inicio de sesión UADE.
 * Valida legajo, email y password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUadeRequest {

    @NotBlank(message = "El legajo es obligatorio")
    private String legajo;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

