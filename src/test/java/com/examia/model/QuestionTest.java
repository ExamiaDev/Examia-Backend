package com.examia.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Question model.
 */
class QuestionTest {

    @Test
    void builder_createsQuestionWithAllFields() {
        Map<String, String> matchingPairs = new HashMap<>();
        matchingPairs.put("A", "1");
        matchingPairs.put("B", "2");

        Question question = Question.builder()
                .id("q-123")
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("What is 2 + 2?")
                .options(Arrays.asList("3", "4", "5", "6"))
                .correctAnswers(Collections.singletonList(1))
                .correctTextAnswer("four")
                .matchingPairs(matchingPairs)
                .correctOrder(Arrays.asList("A", "B", "C"))
                .points(10.0)
                .explanation("Basic math")
                .imageUrl("http://example.com/image.png")
                .order(1)
                .required(true)
                .build();

        assertEquals("q-123", question.getId());
        assertEquals(QuestionType.MULTIPLE_CHOICE, question.getType());
        assertEquals("What is 2 + 2?", question.getText());
        assertEquals(4, question.getOptions().size());
        assertEquals(Collections.singletonList(1), question.getCorrectAnswers());
        assertEquals("four", question.getCorrectTextAnswer());
        assertEquals(matchingPairs, question.getMatchingPairs());
        assertEquals(Arrays.asList("A", "B", "C"), question.getCorrectOrder());
        assertEquals(10.0, question.getPoints());
        assertEquals("Basic math", question.getExplanation());
        assertEquals("http://example.com/image.png", question.getImageUrl());
        assertEquals(1, question.getOrder());
        assertTrue(question.isRequired());
    }

    @Test
    void defaultValues_areSetCorrectly() {
        Question question = Question.builder()
                .text("Test question")
                .build();

        assertTrue(question.isRequired());
    }

    @Test
    void noArgsConstructor_createsEmptyQuestion() {
        Question question = new Question();
        assertNull(question.getId());
        assertNull(question.getText());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        Question question = new Question();
        question.setId("q-123");
        question.setText("New question");
        question.setType(QuestionType.TRUE_FALSE);
        question.setPoints(5.0);
        question.setRequired(false);

        assertEquals("q-123", question.getId());
        assertEquals("New question", question.getText());
        assertEquals(QuestionType.TRUE_FALSE, question.getType());
        assertEquals(5.0, question.getPoints());
        assertFalse(question.isRequired());
    }

    @Test
    void multipleChoiceQuestion_createdCorrectly() {
        Question question = Question.builder()
                .type(QuestionType.MULTIPLE_CHOICE)
                .text("Choose the correct answer")
                .options(Arrays.asList("A", "B", "C", "D"))
                .correctAnswers(Collections.singletonList(2))
                .points(5.0)
                .build();

        assertEquals(QuestionType.MULTIPLE_CHOICE, question.getType());
        assertEquals(4, question.getOptions().size());
        assertEquals(Integer.valueOf(2), question.getCorrectAnswers().get(0));
    }

    @Test
    void multipleSelectionQuestion_createdCorrectly() {
        Question question = Question.builder()
                .type(QuestionType.MULTIPLE_SELECTION)
                .text("Choose all correct answers")
                .options(Arrays.asList("A", "B", "C", "D"))
                .correctAnswers(Arrays.asList(0, 2, 3))
                .points(10.0)
                .build();

        assertEquals(QuestionType.MULTIPLE_SELECTION, question.getType());
        assertEquals(3, question.getCorrectAnswers().size());
    }

    @Test
    void trueFalseQuestion_createdCorrectly() {
        Question question = Question.builder()
                .type(QuestionType.TRUE_FALSE)
                .text("The sky is blue")
                .options(Arrays.asList("True", "False"))
                .correctAnswers(Collections.singletonList(0))
                .build();

        assertEquals(QuestionType.TRUE_FALSE, question.getType());
        assertEquals(2, question.getOptions().size());
    }

    @Test
    void shortAnswerQuestion_createdCorrectly() {
        Question question = Question.builder()
                .type(QuestionType.SHORT_ANSWER)
                .text("What is the capital of France?")
                .correctTextAnswer("Paris")
                .build();

        assertEquals(QuestionType.SHORT_ANSWER, question.getType());
        assertEquals("Paris", question.getCorrectTextAnswer());
    }

    @Test
    void matchingQuestion_createdCorrectly() {
        Map<String, String> pairs = new HashMap<>();
        pairs.put("France", "Paris");
        pairs.put("Germany", "Berlin");
        pairs.put("Spain", "Madrid");

        Question question = Question.builder()
                .type(QuestionType.MATCHING)
                .text("Match countries with their capitals")
                .matchingPairs(pairs)
                .build();

        assertEquals(QuestionType.MATCHING, question.getType());
        assertEquals(3, question.getMatchingPairs().size());
        assertEquals("Paris", question.getMatchingPairs().get("France"));
    }

    @Test
    void orderingQuestion_createdCorrectly() {
        Question question = Question.builder()
                .type(QuestionType.ORDERING)
                .text("Order from smallest to largest")
                .correctOrder(Arrays.asList("1", "5", "10", "50"))
                .build();

        assertEquals(QuestionType.ORDERING, question.getType());
        assertEquals(4, question.getCorrectOrder().size());
        assertEquals("1", question.getCorrectOrder().get(0));
    }

    @Test
    void equals_worksCorrectly() {
        Question q1 = Question.builder().id("q-123").text("Test").build();
        Question q2 = Question.builder().id("q-123").text("Test").build();
        Question q3 = Question.builder().id("q-456").text("Test").build();

        assertEquals(q1, q2);
        assertNotEquals(q1, q3);
    }

    @Test
    void hashCode_worksCorrectly() {
        Question q1 = Question.builder().id("q-123").text("Test").build();
        Question q2 = Question.builder().id("q-123").text("Test").build();

        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void toString_returnsString() {
        Question question = Question.builder().id("q-123").text("Test").build();
        String str = question.toString();

        assertTrue(str.contains("q-123"));
        assertTrue(str.contains("Test"));
    }
}

