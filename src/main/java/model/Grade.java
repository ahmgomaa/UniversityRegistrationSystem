package model;

import model.enums.LetterGrade;

public class Grade {

    public static final int PASS_MARK = 60;

    private final int score;

    public Grade(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getLetterGrade() {
        return LetterGrade.fromScore(score).display();
    }

    public double getGradePoints() {
        return LetterGrade.fromScore(score).getPoints();
    }

    public boolean isPassed() {
        return score >= PASS_MARK;
    }

    @Override
    public String toString() {
        return score + " (" + getLetterGrade() + ")";
    }
}
