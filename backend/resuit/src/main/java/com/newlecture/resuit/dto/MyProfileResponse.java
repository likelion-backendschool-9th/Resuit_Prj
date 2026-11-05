package com.newlecture.resuit.dto;

import java.util.List;

public record MyProfileResponse(ProfileResponse profile, String email,
        List<OwnedResumeSummary> resumes) {
    public record OwnedResumeSummary(Long id, String title, String field,
            boolean isPublic) {
    }
}
