package com.newlecture.resuit.dto;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.entity.Region;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// field, page, size는 무료(9.1)이고, 나머지 상세 필터(9.2)는 기업 Pro만 쓸 수 있다.
public record TalentSearchCondition(
		// 개발 단계라 허용 값 검사는 하지 않는다. 화면의 탭에서 정해진 값만 보낸다.
		String field,
		@Pattern(regexp = "active") String badge,
		@Min(1) Integer streak,
		@Size(max = 10) List<String> skills,
		// [Pro] 경력 구간. ENTRY: 1년 미만(신입), ONE_TO_THREE: 1년 이상 3년 미만, THREE_PLUS: 3년 이상
		@Pattern(regexp = "ENTRY|ONE_TO_THREE|THREE_PLUS") String career,
		@Min(0) Integer minContrib,
		@Min(0) Integer minPosts,
		Region region,
		@Min(0) Integer page,
		@Min(1) @Max(100) Integer size) {

	public Pageable toPageable() {
		return PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
	}

	public boolean hasDetailFilter() {
		return badge != null || streak != null || (skills != null && !skills.isEmpty())
				|| career != null || minContrib != null || minPosts != null || region != null;
	}
}
