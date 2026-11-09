package com.newlecture.resuit.dto.resume;

import java.util.Map;

// 공통 이력서 생성·수정 요청. 수정(PATCH)에서는 null인 값을 바꾸지 않는다.
// photoUrl: null이면 그대로, ""이면 삭제, 업로드한 임시 경로(tmpImage/...)면 저장
public record CommonResumeRequest(String title, String field, Map<String, Object> content, String photoUrl) {}
