package com.newlecture.resuit.dto;

public record ReviewLikeCommand(Long reviewId, String username, boolean liked) {
}
