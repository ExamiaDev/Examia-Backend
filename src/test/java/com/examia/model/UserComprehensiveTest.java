package com.examia.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserComprehensiveTest {

    @Test
    void userBuilderAndGetters() {
        User user = User.builder()
                .id("123")
                .email("test@ejemplo.com")
                .password("encoded-password")
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .legajo("456789")
                .recoveryEmail("recovery@ejemplo.com")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        assertEquals("123", user.getId());
        assertEquals("test@ejemplo.com", user.getEmail());
        assertEquals("encoded-password", user.getPassword());
        assertEquals("Juan", user.getNombre());
        assertEquals("Perez", user.getApellido());
        assertEquals("juanperez", user.getUsername());
        assertEquals("456789", user.getLegajo());
        assertEquals("recovery@ejemplo.com", user.getRecoveryEmail());
        assertEquals(Role.ALUMNO, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void userGetAuthorities() {
        User user = User.builder()
                .role(Role.ALUMNO)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ALUMNO")));
    }

    @Test
    void userGetUsernameReturnsUsername() {
        User user = User.builder()
                .username("juanperez")
                .build();

        assertEquals("juanperez", user.getUsername());
    }

    @Test
    void userIsAccountNonExpired() {
        User user = User.builder().build();
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void userIsAccountNonLocked() {
        User user = User.builder().build();
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void userIsCredentialsNonExpired() {
        User user = User.builder().build();
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void userIsEnabledReturnsFalseWhenDisabled() {
        User user = User.builder()
                .enabled(false)
                .build();

        assertFalse(user.isEnabled());
    }

    @Test
    void userSetters() {
        User user = new User();
        user.setId("789");
        user.setEmail("nuevo@ejemplo.com");
        user.setPassword("new-password");
        user.setNombre("Pedro");
        user.setApellido("Lopez");
        user.setUsername("pedrolopez");
        user.setLegajo("123456");
        user.setRecoveryEmail("recovery@nuevo.com");
        user.setRole(Role.PROFESOR);
        user.setEnabled(true);

        assertEquals("789", user.getId());
        assertEquals("nuevo@ejemplo.com", user.getEmail());
        assertEquals("new-password", user.getPassword());
        assertEquals("Pedro", user.getNombre());
        assertEquals("Lopez", user.getApellido());
        assertEquals("pedrolopez", user.getUsername());
        assertEquals("123456", user.getLegajo());
        assertEquals("recovery@nuevo.com", user.getRecoveryEmail());
        assertEquals(Role.PROFESOR, user.getRole());
        assertTrue(user.isEnabled());
    }
}
