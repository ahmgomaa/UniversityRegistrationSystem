package com.university.registration.model.enums;

/**
 * Letter grades with their grade points and minimum scores.
 * Scale: A &gt;= 90, B+ &gt;= 85, B &gt;= 80, C+ &gt;= 75, C &gt;= 70,
 *        D+ &gt;= 65, D &gt;= 60, otherwise F.
 */
public enum LetterGrade {
    A(4.0, 90),
    B_PLUS(3.5, 85),
    B(3.0, 80),
    C_PLUS(2.5, 75),
    C(2.0, 70),
    D_PLUS(1.5, 65),
    D(1.0, 60),
    F(0.0, 0);

    private final double points;
    private final int minScore;

    LetterGrade(double points, int minScore) {
        this.points = points;
        this.minScore = minScore;
    }

    /** Grade points used in GPA/CGPA calculation. */
    public double getPoints() {
        return points;
    }

    /** Minimum score (inclusive) that maps to this letter grade. */
    public int getMinScore() {
        return minScore;
    }

    /** Maps a numeric score to its letter grade. */
    public static LetterGrade fromScore(double score) {
        for (LetterGrade g : values()) {  // descending order: A first, F last
            if (score >= g.minScore) {
                return g;
            }
        }
        return F;
    }

    /** Human-friendly display, e.g. B_PLUS -&gt; B+. */
    public String display() {
        return name().replace("_PLUS", "+");
    }
}
