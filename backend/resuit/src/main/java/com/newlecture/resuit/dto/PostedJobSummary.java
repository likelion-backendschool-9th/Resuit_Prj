package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.Job;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostedJobSummary(Long id, String title, String type, String status,
        LocalDate deadline, LocalDateTime createdAt, long applicationCount) {
    public static PostedJobSummary from(Job job, long count) {
        return new PostedJobSummary(job.getId(), job.getTitle(), job.getType(), job.getStatus(),
            job.getDeadline(), job.getCreatedAt(), count);
    }
}
