package com.examia.repository;

import com.examia.model.Exam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de exámenes en MongoDB.
 */
@Repository
public interface ExamRepository extends MongoRepository<Exam, String> {

    /**
     * Busca todos los exámenes activos de un profesor.
     *
     * @param professorId el ID del profesor
     * @return lista de exámenes activos del profesor
     */
    List<Exam> findByProfessorIdAndActiveTrue(String professorId);

    /**
     * Busca todos los exámenes activos de una materia.
     *
     * @param subjectId el ID de la materia
     * @return lista de exámenes activos de la materia
     */
    List<Exam> findBySubjectIdAndActiveTrue(String subjectId);

    /**
     * Busca todos los exámenes activos de un profesor para una materia específica.
     *
     * @param professorId el ID del profesor
     * @param subjectId el ID de la materia
     * @return lista de exámenes activos
     */
    List<Exam> findByProfessorIdAndSubjectIdAndActiveTrue(String professorId, String subjectId);

    /**
     * Busca un examen activo por su ID.
     *
     * @param id el ID del examen
     * @return Optional con el examen si existe y está activo
     */
    Optional<Exam> findByIdAndActiveTrue(String id);

    /**
     * Busca todos los exámenes publicados y activos de una materia.
     * Útil para que los alumnos vean los exámenes disponibles.
     *
     * @param subjectId el ID de la materia
     * @return lista de exámenes publicados y activos
     */
    List<Exam> findBySubjectIdAndPublishedTrueAndActiveTrue(String subjectId);

    /**
     * Verifica si existe un examen activo con el título dado para un profesor y materia.
     *
     * @param title el título del examen
     * @param professorId el ID del profesor
     * @param subjectId el ID de la materia
     * @return true si existe un examen con ese título
     */
    boolean existsByTitleAndProfessorIdAndSubjectIdAndActiveTrue(
            String title, String professorId, String subjectId);

    /**
     * Cuenta los exámenes activos de un profesor.
     *
     * @param professorId el ID del profesor
     * @return número de exámenes activos
     */
    long countByProfessorIdAndActiveTrue(String professorId);

    /**
     * Busca todos los exámenes activos.
     *
     * @return lista de todos los exámenes activos
     */
    List<Exam> findByActiveTrue();
}

