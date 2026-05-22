package com.examia.repository;

import com.examia.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de usuarios en MongoDB.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Busca un usuario por su email.
     *
     * @param email el email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email el email a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    boolean existsByDisplayUsername(String displayUsername);

    /**
     * Busca un usuario por su legajo.
     *
     * @param legajo el legajo del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByLegajo(String legajo);

    /**
     * Verifica si existe un usuario con el legajo dado.
     *
     * @param legajo el legajo a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    boolean existsByLegajo(String legajo);
}

