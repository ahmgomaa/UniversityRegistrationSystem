package com.university.registration.model.enums;

/**
 * Represents letter grades with their associated grade point values and score thresholds.
 */
public enum LetterGrade {
    A_PLUS(4.0, 90),
    A(4.0, 85),
    B_PLUS(3.5, 80),
    B(3.0, 75),
    C_PLUS(2.5, 70),
    C(2.0, 65),
    D_PLUS(1.5, 60),
    D(1.0, 50),
    F(0.0, 0);

    private final double gradePoints;
    private final int minScore;

    LetterGrade(double gradePoints, int minScore) {
        this.gradePoints = gradePoints;
        this.minScore = minScore;
    }

    public double getGradePoints() {
        return gradePoints;
    }

    public int getMinScore() {
        return minScore;
    }

    /**
     * Determines the maximum score threshold for this letter grade.
     * For A_PLUS, the max is 100. For others, it's one below the next higher grade's minScore.
     *
     * @return the maximum score for this grade
     */
    public int getMaxScore() {
        LetterGrade[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == this) {
                return (i == 0) ? 100 : values[i - 1].minScore - 1;
            }
        }
        return 100;
    }

    /**
     * Converts a numeric score to the corresponding letter grade.
     *
     * @param score the numeric score (0-100)
     * @return the corresponding LetterGrade
     * @throws IllegalArgumentException if the score is outside the valid range
     */
    public static LetterGrade fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100, got: " + score);
        }
        for (LetterGrade grade : values()) {
            if (score >= grade.minScore) {
                return grade;
            }
        }
        return F;
    }
}
