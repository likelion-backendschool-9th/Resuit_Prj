package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.JobQuestion;
import com.newlecture.resuit.entity.TargetResume;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record TargetResumeDetailResponse(
    Long id,
    JobInfo job,
    List<JobInfo.Question> questions,
    Long commonResumeId,
    String title,
    String photoUrl,
    Map<String, Object> content,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TargetResumeDetailResponse of(TargetResume resume, List<JobQuestion> questions, boolean submitted) {
        var base = resume.getCommonResume();
        return new TargetResumeDetailResponse(resume.getId(), JobInfo.from(resume.getJob()),
            questions.stream().map(JobInfo.Question::from).toList(),
            base == null ? null : base.getId(), resume.getTitle(), resume.getPhotoUrl(), resume.getContent(),
            submitted ? "SUBMITTED" : "WRITING", resume.getCreatedAt(), resume.getUpdatedAt());
    }
}
