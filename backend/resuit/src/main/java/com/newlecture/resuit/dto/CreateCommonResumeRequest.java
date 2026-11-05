package com.newlecture.resuit.dto;

import java.util.Map;

public record CreateCommonResumeRequest(String title, String field, Map<String, Object> content) {
}
