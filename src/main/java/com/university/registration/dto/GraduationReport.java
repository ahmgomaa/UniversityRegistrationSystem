package com.university.registration.dto;

import java.util.List;

/** Outcome of a graduation eligibility check. */
public record GraduationReport(boolean eligible, List<String> unmetReasons) {

    public GraduationReport {
        unmetReasons = List.copyOf(unmetReasons == null ? List.of() : unmetReasons);
    }
}
