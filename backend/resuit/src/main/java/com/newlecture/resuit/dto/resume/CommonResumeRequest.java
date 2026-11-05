package com.newlecture.resuit.dto.resume;

import java.util.Map;

// 공통 이력서 생성·수정 요청. 수정(PATCH)에서는 null인 값을 바꾸지 않는다.
public record CommonResumeRequest(String title, String field, Map<String, Object> content) {}
