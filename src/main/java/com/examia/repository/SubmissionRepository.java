package com.examia.repository;

import com.examia.model.Submission;
import com.examia.model.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends MongoRepository<Submission, String> {

    List<Submission> findByExamIdAndActiveTrue(String examId);

    Optional<Submission> findByIdAndActiveTrue(String id);

    Optional<Submission> findByExamIdAndStudentIdAndActiveTrue(String examId, String studentId);

    List<Submission> findByStudentIdAndActiveTrue(String studentId);

    long countByExamIdAndActiveTrue(String examId);

    long countByExamIdAndStatusAndActiveTrue(String examId, SubmissionStatus status);
}
