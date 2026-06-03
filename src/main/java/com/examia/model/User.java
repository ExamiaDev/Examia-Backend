package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario en el sistema Examia.
 * Implementa UserDetails para integrarse con Spring Security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String nombre;

    private String apellido;

    /**
     * Nombre de usuario personalizado (distinto del email usado para autenticación).
     * El método getUsername() de UserDetails retorna el email para Spring Security.
     */
    @Indexed(unique = true, sparse = true)
    private String displayUsername;

    @Indexed(unique = true, sparse = true)
    private String legajo;

    private String recoveryEmail;

    private Role role;

    @CreatedDate
    private LocalDateTime createdAt;

    private boolean enabled;

    private int failedAttempts;

    private LocalDateTime lockedUntil;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;  // Usamos email como identificador principal para Spring Security
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

