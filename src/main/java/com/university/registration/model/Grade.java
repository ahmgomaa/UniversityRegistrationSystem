package com.university.registration.model;

import com.university.registration.model.enums.LetterGrade;

/**
 * Numeric grade for a completed enrollment, from which the letter grade and
 * grade points are derived.
 */
public class Grade {

    /** Minimum passing score. */
    public static final double PASS_MARK = 60.0;

    private final double score;

    public Grade(double score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100, got " + score);
        }
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public LetterGrade getLetterGrade() {
        return LetterGrade.fromScore(score);
    }

    public double getGradePoints() {
        return getLetterGrade().getPoints();
    }

    public boolean isPassed() {
        return score >= PASS_MARK;
    }

    @Override
    public String toString() {
        return "%.0f (%s)".formatted(score, getLetterGrade().display());
    }
}
