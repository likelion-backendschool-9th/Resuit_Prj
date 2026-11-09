package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;

// 이력서 사진 값을 저장할 주소로 바꾼다. 공통·특화 이력서가 같이 쓴다.
// null: 바꾸지 않음, 빈 문자열: 사진 삭제, tmpImage/...: 내가 올린 임시 이미지를 정식 폴더로 옮김,
// /media/image/{내 회원 ID}/...: 이미 저장된 내 사진을 그대로 씀. 그 밖의 값(외부 주소, 남의 사진)은 받지 않는다.
@Component
public class ResumePhotoResolver {

	private static final String PUBLISHED = "/media/image/";

	private final MediaService mediaService;

	public ResumePhotoResolver(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	// 반환값: null이면 그대로 두고, ""이면 지우고, 그 밖에는 저장할 주소다.
	public String resolve(UUID userId, String raw) {
		if (raw == null) {
			return null;
		}
		String value = raw.trim();
		if (value.isEmpty()) {
			return "";
		}
		if (value.startsWith(PUBLISHED + userId + "/")) {
			return value;
		}
		String relative = value.startsWith("/media/") ? value.substring("/media/".length()) : value;
		if (!relative.startsWith("tmpImage/")) {
			throw new InvalidResumeException("이력서 사진은 사진 업로드로 올린 이미지만 쓸 수 있어요.");
		}
		try {
			return "/media/" + mediaService.finalizeForPost(relative, userId, "IMAGE");
		} catch (IllegalArgumentException exception) {
			throw new InvalidResumeException("사진을 저장하지 못했어요. 다시 올려 주세요.");
		}
	}
}
