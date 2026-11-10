package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.JobQuestion;
import com.newlecture.resuit.entity.TargetResume;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// feedbackRequestId: 진행 중인(취소하지 않은) 피드백 요청. 없으면 null
// feedbackRequestHasComments: 그 요청에 댓글이 달려 취소할 수 없는지
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
    LocalDateTime updatedAt,
    Long feedbackRequestId,
    boolean feedbackRequestHasComments
) {
    public static TargetResumeDetailResponse of(TargetResume resume, List<JobQuestion> questions, boolean submitted,
                                                Long feedbackRequestId, boolean feedbackRequestHasComments) {
        var base = resume.getCommonResume();
        return new TargetResumeDetailResponse(resume.getId(), JobInfo.from(resume.getJob()),
            questions.stream().map(JobInfo.Question::from).toList(),
            base == null ? null : base.getId(), resume.getTitle(), resume.getPhotoUrl(), resume.getContent(),
            submitted ? "SUBMITTED" : "WRITING", resume.getCreatedAt(), resume.getUpdatedAt(),
            feedbackRequestId, feedbackRequestHasComments);
    }
}
