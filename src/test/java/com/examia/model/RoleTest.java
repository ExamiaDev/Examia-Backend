Sopackage com.examia.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void roleEnumValues() {
        assertEquals(3, Role.values().length);
        assertTrue(contains(Role.values(), Role.ALUMNO));
        assertTrue(contains(Role.values(), Role.PROFESOR));
        assertTrue(contains(Role.values(), Role.ADMIN));
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
    void roleAdminValue() {
        assertEquals("ADMIN", Role.ADMIN.name());
    }

    @Test
    void roleValueOf() {
        assertEquals(Role.ALUMNO, Role.valueOf("ALUMNO"));
        assertEquals(Role.PROFESOR, Role.valueOf("PROFESOR"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    private boolean contains(Role[] roles, Role role) {
        for (Role r : roles) {
            if (r == role) return true;
        }
        return false;
    }
}

