package com.newlecture.resuit.dto.feedback;

import com.newlecture.resuit.dto.resume.JobInfo;
import com.newlecture.resuit.entity.FeedbackRequest;
import java.time.LocalDateTime;
import java.util.List;

// 피드백 게시판 카드(4.1). 댓글은 카드를 펼칠 때 따로 불러오고, 여기에는 개수만 담는다.
// resumeType: COMMON, TARGET. 미리보기(4.5)는 resumeType과 resumeId로 부른다.
// companyName·jobTitle: 특화 이력서일 때 지원 공고. 공통 이력서면 null. companyName은 본인 요청에만 채운다
public record FeedbackRequestSummary(
    Long id,
    FeedbackAuthor author,
    String category,
    String resumeType,
    Long resumeId,
    String resumeTitle,
    String companyName,
    String jobTitle,
    String content,
    List<String> sections,
    long commentCount,
    boolean adopted,
    boolean mine,
    LocalDateTime createdAt
) {
    public static FeedbackRequestSummary of(FeedbackRequest request, List<String> sections,
            long commentCount, boolean adopted, boolean mine) {
        var common = request.getCommonResume();
        var target = request.getTargetResume();
        var job = target == null ? null : target.getJob();
        return new FeedbackRequestSummary(request.getId(), FeedbackAuthor.of(request.getUser()), request.getCategory(),
            common != null ? "COMMON" : "TARGET",
            common != null ? common.getId() : target.getId(),
            common != null ? common.getTitle() : target.getTitle(),
            // 회사 이름은 요청한 본인에게만 보여 준다
            job == null || !mine ? null : JobInfo.from(job).companyName(),
            job == null ? null : job.getTitle(),
            request.getContent(), sections, commentCount, adopted, mine, request.getCreatedAt());
    }
}
