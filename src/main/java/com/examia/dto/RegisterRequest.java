package com.examia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Pattern(regexp = "^\\S+$", message = "El nombre de usuario no puede contener espacios")
    private String username;

    @NotBlank(message = "El mail principal es obligatorio")
    @Email(message = "El formato del mail principal no es válido")
    private String email;

    @NotBlank(message = "El mail de recupero es obligatorio")
    @Email(message = "El formato del mail de recupero no es válido")
    private String recoveryEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
