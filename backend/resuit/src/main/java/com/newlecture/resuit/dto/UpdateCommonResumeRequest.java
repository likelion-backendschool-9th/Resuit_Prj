package com.newlecture.resuit.dto;

import java.util.Map;

public record UpdateCommonResumeRequest(String title, String field, Map<String, Object> content,
        Boolean isPublic) {
}
