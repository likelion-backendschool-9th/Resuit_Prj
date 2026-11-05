package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.DisplayedBadgeRequest;
import com.newlecture.resuit.dto.FollowStatusResponse;
import com.newlecture.resuit.dto.MyProfileResponse;
import com.newlecture.resuit.dto.PageResponse;
import com.newlecture.resuit.dto.PinnedResumeRequest;
import com.newlecture.resuit.dto.ProfileFeedbackResponse;
import com.newlecture.resuit.dto.ProfilePostResponse;
import com.newlecture.resuit.dto.ProfileResponse;
import com.newlecture.resuit.dto.ResumeVisibilityRequest;
import com.newlecture.resuit.dto.UpdateProfileRequest;
import java.util.UUID;

public interface ProfileService {
    ProfileResponse getPublicProfile(String username, UUID viewerId);
    MyProfileResponse getMyProfile(UUID userId);
    PageResponse<ProfilePostResponse> getPosts(String username, String type, int page, int size);
    PageResponse<ProfileFeedbackResponse> getFeedbacks(String username, int page, int size);
    MyProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request);
    MyProfileResponse setResumeVisibility(UUID userId, Long resumeId, ResumeVisibilityRequest request);
    ProfileResponse pinResume(UUID userId, PinnedResumeRequest request);
    ProfileResponse displayBadge(UUID userId, DisplayedBadgeRequest request);
    FollowStatusResponse follow(UUID followerId, String targetUsername);
    FollowStatusResponse unfollow(UUID followerId, String targetUsername);
}
