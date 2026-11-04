package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.CommonResume;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// 이력서 탭 목록 카드. 내용 전체 대신 섹션별 개수만 보낸다.
public record CommonResumeSummary(
    Long id,
    String title,
    String field,
    boolean isPublic,
    List<Section> sections,
    long feedbackCount,
    long targetResumeCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // type: BASIC, SKILLS, PROJECTS, AWARDS, INTRODUCTION (docs/database-design.md 이력서 항목)
    public record Section(String type, int count) {}

    public static CommonResumeSummary of(CommonResume resume, long feedbackCount, long targetResumeCount) {
        return new CommonResumeSummary(resume.getId(), resume.getTitle(), resume.getField(), resume.isPublic(),
            sectionsOf(resume.getContent()), feedbackCount, targetResumeCount,
            resume.getCreatedAt(), resume.getUpdatedAt());
    }

    private static List<Section> sectionsOf(Map<String, Object> content) {
        List<Section> sections = new ArrayList<>();
        if (content == null) return sections;
        addIfPresent(sections, "BASIC", content.get("basic"));
        addIfPresent(sections, "SKILLS", content.get("skills"));
        addIfPresent(sections, "PROJECTS", content.get("projects"));
        addIfPresent(sections, "AWARDS", content.get("awards"));
        addIfPresent(sections, "INTRODUCTION", content.get("introduction"));
        return sections;
    }

    private static void addIfPresent(List<Section> sections, String type, Object value) {
        int count = switch (value) {
            case Collection<?> list -> list.size();
            case Map<?, ?> map -> map.isEmpty() ? 0 : 1;
            case String text -> text.isBlank() ? 0 : 1;
            case null, default -> 0;
        };
        if (count > 0) sections.add(new Section(type, count));
    }
}
