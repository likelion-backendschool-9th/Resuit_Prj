package com.newlecture.resuit.dto;

import java.time.LocalDate;

public record CareerRequest(String companyName, String jobTitle, LocalDate startDate,
        LocalDate endDate, String description) {
}
