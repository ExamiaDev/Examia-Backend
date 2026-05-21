package com.examia.config;

import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationConfigTest {

    @Test
    void userDetailsServiceWhenUserExistsShouldReturnUserDetails() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = User.builder()
                .username("usuario@ejemplo.com")
                .password("encoded-password")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();
        ApplicationConfig config = new ApplicationConfig(userRepository);

        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = config.userDetailsService().loadUserByUsername("usuario@ejemplo.com");

        assertEquals("usuario@ejemplo.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void userDetailsServiceWhenUserDoesNotExistShouldThrowUsernameNotFoundException() {
        UserRepository userRepository = mock(UserRepository.class);
        ApplicationConfig config = new ApplicationConfig(userRepository);
        when(userRepository.findByEmail("missing@ejemplo.com")).thenReturn(Optional.empty());
        var userDetailsService = config.userDetailsService();

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@ejemplo.com")
        );

        assertTrue(exception.getMessage().contains("missing@ejemplo.com"));
    }

    @Test
    void passwordEncoderShouldUseBCrypt() {
        UserRepository userRepository = mock(UserRepository.class);
        ApplicationConfig config = new ApplicationConfig(userRepository);

        assertInstanceOf(BCryptPasswordEncoder.class, config.passwordEncoder());
    }
}
