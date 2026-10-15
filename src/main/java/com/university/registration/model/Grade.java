package com.university.registration.model;

import com.university.registration.model.enums.LetterGrade;

/**
 * Represents a numeric grade for a course.
 * Encapsulates score-to-letter-grade conversion logic.
 */
public class Grade {

    /** The minimum passing score. */
    public static final int PASS_MARK = 50;

    private final int score;

    public Grade(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100, got: " + score);
        }
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    /**
     * Converts the numeric score to a letter grade.
     *
     * @return the corresponding LetterGrade
     */
    public LetterGrade getLetterGrade() {
        return LetterGrade.fromScore(score);
    }

    /**
     * Returns the grade point value for this score.
     *
     * @return the grade points (e.g., 4.0 for an A)
     */
    public double getGradePoints() {
        return getLetterGrade().getGradePoints();
    }

    /**
     * Checks whether this grade is a passing grade.
     *
     * @return true if the score meets or exceeds the pass mark
     */
    public boolean isPassing() {
        return score >= PASS_MARK;
    }

    @Override
    public String toString() {
        return "Grade{score=" + score + ", letter=" + getLetterGrade() + "}";
    }
}
