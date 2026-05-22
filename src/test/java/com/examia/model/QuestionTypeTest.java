package com.examia.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QuestionType enum.
 */
class QuestionTypeTest {

    @Test
    void allQuestionTypes_exist() {
        assertEquals(8, QuestionType.values().length);
    }

    @Test
    void multipleChoice_exists() {
        assertEquals("MULTIPLE_CHOICE", QuestionType.MULTIPLE_CHOICE.name());
    }

    @Test
    void multipleSelection_exists() {
        assertEquals("MULTIPLE_SELECTION", QuestionType.MULTIPLE_SELECTION.name());
    }

    @Test
    void trueFalse_exists() {
        assertEquals("TRUE_FALSE", QuestionType.TRUE_FALSE.name());
    }

    @Test
    void shortAnswer_exists() {
        assertEquals("SHORT_ANSWER", QuestionType.SHORT_ANSWER.name());
    }

    @Test
    void longAnswer_exists() {
        assertEquals("LONG_ANSWER", QuestionType.LONG_ANSWER.name());
    }

    @Test
    void fillInTheBlank_exists() {
        assertEquals("FILL_IN_THE_BLANK", QuestionType.FILL_IN_THE_BLANK.name());
    }

    @Test
    void ordering_exists() {
        assertEquals("ORDERING", QuestionType.ORDERING.name());
    }

    @Test
    void matching_exists() {
        assertEquals("MATCHING", QuestionType.MATCHING.name());
    }

    @Test
    void valueOf_returnsCorrectType() {
        assertEquals(QuestionType.MULTIPLE_CHOICE, QuestionType.valueOf("MULTIPLE_CHOICE"));
        assertEquals(QuestionType.TRUE_FALSE, QuestionType.valueOf("TRUE_FALSE"));
    }

    @Test
    void valueOf_invalidType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> QuestionType.valueOf("INVALID_TYPE"));
    }
}

