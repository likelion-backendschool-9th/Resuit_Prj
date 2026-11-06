package com.newlecture.resuit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.newlecture.resuit.entity.Job;

public record JobResponse(
		Long id,
		Company company,
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
		LocalDateTime createdAt) {

	public static JobResponse from(Job job) {
		return new JobResponse(
				job.getId(),
				new Company(
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
				job.getCreatedAt());
	}

	public record Company(
			String username,
			String nickname,
			String profileImageUrl) {
	}
}
