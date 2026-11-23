package model.enums;

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

    public double getPoints() {
        return points;
    }

    public int getMinScore() {
        return minScore;
    }

    public static LetterGrade fromScore(double score) {
        for (LetterGrade g : values()) {
            if (score >= g.minScore) {
                return g;
            }
        }
        return F;
    }

    public String display() {
        return name().replace("_PLUS", "+");
    }
}
