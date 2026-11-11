package com.newlecture.resuit.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank @Pattern(regexp = "(?i)GENERAL|SPECIAL") String type,
        @NotBlank @Size(max = 100) String title,
        @Size(max = 50) String location,
        @Size(max = 30) String employmentType,
        @Size(max = 30) String career,
        String description,
        String requirements,
        String preferred,
        @FutureOrPresent LocalDate deadline,
        List<@NotNull @Valid Question> questions) {

    public record Question(
            @NotBlank @Size(max = 300) String content,
            @Positive Integer charLimit) {
    }
}
