package com.newlecture.resuit.dto;

import java.util.List;
import java.util.Map;

// 인재 목록(9.1) 응답이다. 한 페이지의 인재와 함께 분야 탭 옆에 보여 줄 인원 수를 담는다.
// 탭마다 요청을 따로 보내지 않도록 인원 수를 목록 응답에 같이 담는다.
public record TalentPageResponse(
		List<TalentSummary> content,
		int page,
		int size,
		long totalElements,
		boolean hasNext,
		// 분야별 인원. 키는 ALL(전체)과 분야 코드(BACKEND 등)이다. 상세 필터는 반영하고 분야 조건만 뺀 값이다.
		Map<String, Long> fieldCounts) {
}
