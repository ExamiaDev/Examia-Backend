package com.examia.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void roleEnumValues() {
        assertEquals(2, Role.values().length);
        assertTrue(contains(Role.values(), Role.ALUMNO));
        assertTrue(contains(Role.values(), Role.PROFESOR));
    }

    @Test
    void roleAlumnoValue() {
        assertEquals("ALUMNO", Role.ALUMNO.name());
    }

    @Test
    void roleProfesorValue() {
        assertEquals("PROFESOR", Role.PROFESOR.name());
    }

    @Test
    void roleValueOf() {
        assertEquals(Role.ALUMNO, Role.valueOf("ALUMNO"));
        assertEquals(Role.PROFESOR, Role.valueOf("PROFESOR"));
    }

    private boolean contains(Role[] roles, Role role) {
        for (Role r : roles) {
            if (r == role) return true;
        }
        return false;
    }
}
