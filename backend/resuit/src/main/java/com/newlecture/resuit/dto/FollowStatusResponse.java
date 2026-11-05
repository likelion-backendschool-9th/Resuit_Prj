package com.newlecture.resuit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowStatusResponse(
        @JsonProperty("isFollowing") boolean isFollowing,
        long followerCount,
        long followingCount) {
}
