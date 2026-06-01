package com.examia.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exam model.
 */
class ExamTest {

    @Test
    void builder_createsExamWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Question question = Question.builder()
                .id("q-123")
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("Test question")
                .points(10.0)
                .build();

        Exam exam = Exam.builder()
                .id("exam-123")
                .title("Test Exam")
                .description("Test description")
                .professorId("prof-123")
                .subjectId("subject-123")
                .subjectName("Test Subject")
                .questions(Collections.singletonList(question))
                .durationMinutes(60)
                .totalPoints(10.0)
                .passingScore(60.0)
                .scheduledStartTime(now)
                .scheduledEndTime(now.plusHours(1))
                .published(true)
                .shuffleQuestions(true)
                .shuffleOptions(true)
                .showResultsOnCompletion(false)
                .maxAttempts(3)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("exam-123", exam.getId());
        assertEquals("Test Exam", exam.getTitle());
        assertEquals("Test description", exam.getDescription());
        assertEquals("prof-123", exam.getProfessorId());
        assertEquals("subject-123", exam.getSubjectId());
        assertEquals("Test Subject", exam.getSubjectName());
        assertEquals(1, exam.getQuestions().size());
        assertEquals(60, exam.getDurationMinutes());
        assertEquals(10.0, exam.getTotalPoints());
        assertEquals(60.0, exam.getPassingScore());
        assertEquals(now, exam.getScheduledStartTime());
        assertEquals(now.plusHours(1), exam.getScheduledEndTime());
        assertTrue(exam.isPublished());
        assertTrue(exam.isShuffleQuestions());
        assertTrue(exam.isShuffleOptions());
        assertFalse(exam.isShowResultsOnCompletion());
        assertEquals(3, exam.getMaxAttempts());
        assertTrue(exam.isActive());
        assertEquals(now, exam.getCreatedAt());
        assertEquals(now, exam.getUpdatedAt());
    }

    @Test
    void defaultValues_areSetCorrectly() {
        Exam exam = Exam.builder()
                .title("Test Exam")
                .build();

        assertFalse(exam.isPublished());
        assertFalse(exam.isShuffleQuestions());
        assertFalse(exam.isShuffleOptions());
        assertTrue(exam.isShowResultsOnCompletion());
        assertTrue(exam.isActive());
    }

    @Test
    void noArgsConstructor_createsEmptyExam() {
        Exam exam = new Exam();
        assertNull(exam.getId());
        assertNull(exam.getTitle());
    }

    @Test
    void allArgsConstructor_createsExamWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Exam exam = new Exam(
                "exam-123",
                "Test Exam",
                "Description",
                "prof-123",
                "subject-123",
                "Subject Name",
                Collections.emptyList(),
                60,
                100.0,
                60.0,
                now,
                now.plusHours(1),
                "Mañana",
                true,
                false,
                false,
                true,
                2,
                true,
                now,
                now,
                null
        );

        assertEquals("exam-123", exam.getId());
        assertEquals("Test Exam", exam.getTitle());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        Exam exam = new Exam();
        exam.setId("exam-123");
        exam.setTitle("New Title");
        exam.setActive(false);

        assertEquals("exam-123", exam.getId());
        assertEquals("New Title", exam.getTitle());
        assertFalse(exam.isActive());
    }

    @Test
    void equals_worksCorrectly() {
        Exam exam1 = Exam.builder().id("exam-123").title("Test").build();
        Exam exam2 = Exam.builder().id("exam-123").title("Test").build();
        Exam exam3 = Exam.builder().id("exam-456").title("Test").build();

        assertEquals(exam1, exam2);
        assertNotEquals(exam1, exam3);
    }

    @Test
    void hashCode_worksCorrectly() {
        Exam exam1 = Exam.builder().id("exam-123").title("Test").build();
        Exam exam2 = Exam.builder().id("exam-123").title("Test").build();

        assertEquals(exam1.hashCode(), exam2.hashCode());
    }

    @Test
    void toString_returnsString() {
        Exam exam = Exam.builder().id("exam-123").title("Test").build();
        String str = exam.toString();

        assertTrue(str.contains("exam-123"));
        assertTrue(str.contains("Test"));
    }
}

