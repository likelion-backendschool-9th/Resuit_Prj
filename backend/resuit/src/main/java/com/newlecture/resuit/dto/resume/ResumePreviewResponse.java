package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.dto.feedback.FeedbackAuthor;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.JobQuestion;
import com.newlecture.resuit.entity.TargetResume;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 피드백 게시판의 이력서 미리보기(4.5). 공통·특화 이력서를 같은 모양으로 보낸다.
// job·questions: 특화 이력서일 때만 채운다. 공통 이력서면 null과 빈 목록
public record ResumePreviewResponse(
    String type,
    Long id,
    FeedbackAuthor author,
    String title,
    String field,
    Map<String, Object> content,
    JobInfo job,
    List<JobInfo.Question> questions,
    LocalDateTime updatedAt
) {
    public static ResumePreviewResponse from(CommonResume resume) {
        return new ResumePreviewResponse("COMMON", resume.getId(), FeedbackAuthor.of(resume.getUser()),
            resume.getTitle(), resume.getField(), resume.getContent(), null, List.of(), resume.getUpdatedAt());
    }

    public static ResumePreviewResponse of(TargetResume resume, List<JobQuestion> questions) {
        var base = resume.getCommonResume();
        return new ResumePreviewResponse("TARGET", resume.getId(), FeedbackAuthor.of(resume.getUser()),
            resume.getTitle(), base == null ? null : base.getField(), resume.getContent(),
            JobInfo.from(resume.getJob()), questions.stream().map(JobInfo.Question::from).toList(),
            resume.getUpdatedAt());
    }
}
