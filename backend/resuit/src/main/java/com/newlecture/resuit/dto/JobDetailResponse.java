package com.newlecture.resuit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.newlecture.resuit.entity.Job;

public record JobDetailResponse(
		Long id,
		JobResponse.Company company,
		String type,
		String title,
		String location,
		String employmentType,
		String career,
		String description,
		String requirements,
		String preferred,
		LocalDate deadline,
		String status,
		LocalDateTime createdAt,
		List<Question> questions) {

	public static JobDetailResponse from(Job job, List<Question> questions) {
		return new JobDetailResponse(
				job.getId(),
				new JobResponse.Company(
						job.getCompany().getUsername(),
						job.getCompany().getNickname(),
						job.getCompany().getProfileImageUrl()),
				job.getType(),
				job.getTitle(),
				job.getLocation(),
				job.getEmploymentType(),
				job.getCareer(),
				job.getDescription(),
				job.getRequirements(),
				job.getPreferred(),
				job.getDeadline(),
				job.getStatus(),
				job.getCreatedAt(),
				questions);
	}

	public record Question(
			Long id,
			String content,
			Integer charLimit,
			int orderNo) {
	}
}
