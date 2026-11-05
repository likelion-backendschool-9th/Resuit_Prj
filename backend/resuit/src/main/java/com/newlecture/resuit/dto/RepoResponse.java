package com.newlecture.resuit.dto;

public record RepoResponse(String fullName, String url, String description,
        String language, boolean pinned) {
}
