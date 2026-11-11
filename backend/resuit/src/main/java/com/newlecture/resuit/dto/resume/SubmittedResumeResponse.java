package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.JobApplication;
import java.util.List;
import java.util.Map;

public record SubmittedResumeResponse(SubmittedResumeSummary submission, String field,
        Map<String, Object> content, List<Question> questions, String photoUrl) {
    public record Question(Long id, String content, Integer charLimit, int orderNo) {}
    public static SubmittedResumeResponse from(JobApplication a) {
        return from(a, "/api/v1/users/me/resume-submissions/" + a.getId() + "/photo");
    }
    public static SubmittedResumeResponse from(JobApplication a, String photoUrl) {
        var snapshot = a.getSubmittedResumeSnapshot();
        return new SubmittedResumeResponse(SubmittedResumeSummary.from(a), snapshot.field(), snapshot.content(),
            snapshot.questions().stream().map(q -> new Question(q.id(), q.content(), q.charLimit(), q.orderNo())).toList(),
            a.getSnapshotPhotoKey() == null ? null : photoUrl);
    }
}
