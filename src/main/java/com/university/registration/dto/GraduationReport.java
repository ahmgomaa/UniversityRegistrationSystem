package com.university.registration.dto;

import java.util.Collections;
import java.util.List;

/**
 * Report generated when checking a student's graduation eligibility.
 */
public class GraduationReport {

    private final boolean eligible;
    private final List<String> completedCourses;
    private final List<String> unfulfilledReasons;

    public GraduationReport(boolean eligible, List<String> completedCourses,
                            List<String> unfulfilledReasons) {
        this.eligible = eligible;
        this.completedCourses = completedCourses != null
                ? Collections.unmodifiableList(completedCourses)
                : Collections.emptyList();
        this.unfulfilledReasons = unfulfilledReasons != null
                ? Collections.unmodifiableList(unfulfilledReasons)
                : Collections.emptyList();
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getCompletedCourses() {
        return completedCourses;
    }

    public List<String> getUnfulfilledReasons() {
        return unfulfilledReasons;
    }

    @Override
    public String toString() {
        return "GraduationReport{eligible=" + eligible
                + ", completedCourses=" + completedCourses.size()
                + ", issues=" + unfulfilledReasons.size() + "}";
    }
}
