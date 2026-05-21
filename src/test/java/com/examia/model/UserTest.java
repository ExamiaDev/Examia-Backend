package com.examia.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userDetailsMethodsShouldReturnConfiguredAccountStateAndAuthorities() {
        User user = User.builder()
                .username("usuario@ejemplo.com")
                .password("encoded-password")
                .role(Role.PROFESOR)
                .enabled(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals("usuario@ejemplo.com", user.getUsername());
        assertEquals("encoded-password", user.getPassword());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROFESOR")));
    }

    @Test
    void builderShouldCreateUserWithAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = User.builder()
                .id("user-123")
                .email("test@ejemplo.com")
                .password("password123")
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .legajo("12345")
                .recoveryEmail("recovery@ejemplo.com")
                .role(Role.ALUMNO)
                .createdAt(createdAt)
                .enabled(true)
                .build();

        assertEquals("user-123", user.getId());
        assertEquals("test@ejemplo.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Juan", user.getNombre());
        assertEquals("Perez", user.getApellido());
        assertEquals("12345", user.getLegajo());
        assertEquals("recovery@ejemplo.com", user.getRecoveryEmail());
        assertEquals(Role.ALUMNO, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
        assertTrue(user.isEnabled());
    }

    @Test
    void settersShouldUpdateFields() {
        User user = new User();
        LocalDateTime createdAt = LocalDateTime.now();

        user.setId("id-456");
        user.setEmail("updated@ejemplo.com");
        user.setPassword("newpassword");
        user.setNombre("Maria");
        user.setApellido("Garcia");
        user.setUsername("mariagarcia");
        user.setLegajo("67890");
        user.setRecoveryEmail("newrecovery@ejemplo.com");
        user.setRole(Role.PROFESOR);
        user.setCreatedAt(createdAt);
        user.setEnabled(false);

        assertEquals("id-456", user.getId());
        assertEquals("updated@ejemplo.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("Maria", user.getNombre());
        assertEquals("Garcia", user.getApellido());
        assertEquals("67890", user.getLegajo());
        assertEquals("newrecovery@ejemplo.com", user.getRecoveryEmail());
        assertEquals(Role.PROFESOR, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
        assertFalse(user.isEnabled());
    }

    @Test
    void noArgsConstructorShouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getNombre());
        assertFalse(user.isEnabled());
    }

    @Test
    void allArgsConstructorShouldCreateUser() {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = new User(
                "id-all",
                "all@ejemplo.com",
                "password",
                "Nombre",
                "Apellido",
                "username",
                "legajo",
                "recovery@ejemplo.com",
                Role.ALUMNO,
                createdAt,
                true
        );

        assertEquals("id-all", user.getId());
        assertEquals("all@ejemplo.com", user.getEmail());
        assertEquals("Nombre", user.getNombre());
        assertEquals("Apellido", user.getApellido());
        assertTrue(user.isEnabled());
    }

    @Test
    void equalsShouldWorkCorrectly() {
        User user1 = User.builder()
                .id("123")
                .email("test@test.com")
                .nombre("Test")
                .build();

        User user2 = User.builder()
                .id("123")
                .email("test@test.com")
                .nombre("Test")
                .build();

        User user3 = User.builder()
                .id("456")
                .email("other@test.com")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
    }

    @Test
    void toStringShouldContainFields() {
        User user = User.builder()
                .id("123")
                .email("test@test.com")
                .nombre("Juan")
                .build();

        String str = user.toString();

        assertTrue(str.contains("123"));
        assertTrue(str.contains("test@test.com"));
        assertTrue(str.contains("Juan"));
    }

    @Test
    void getAuthoritiesShouldReturnCorrectRoleForAlumno() {
        User user = User.builder()
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO")));
    }
}
