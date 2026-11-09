package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.MediaUploadResponse;
import com.newlecture.resuit.service.MediaService;

// 이미지 업로드 API. 게시글 업로드(/posts/uploads)와 같은 저장 로직(MediaService)을 쓰지만 이미지만 받는다.
// 게시글 업로드는 게시글 전용으로 바뀔 예정이라, 이력서 사진처럼 게시글이 아닌 곳은 이 API를 쓴다.
// 올린 파일은 임시 폴더(tmpImage)에 있고, 이력서 등을 저장할 때 정식 폴더(image)로 옮긴다.
@RestController
@RequestMapping("/api/v1/media/images")
public class MediaController {

	private final MediaService mediaService;

	public MediaController(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	// 이미지 업로드: POST /api/v1/media/images (multipart, file). 응답 path를 저장 요청에 넣는다.
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public MediaUploadResponse upload(@RequestParam("file") MultipartFile file, Principal principal) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 올릴 수 있습니다.");
		}
		return mediaService.upload(file, principal.getName());
	}

	// 저장하지 않고 취소한 임시 이미지 삭제: DELETE /api/v1/media/images?path=tmpImage/...
	@DeleteMapping
	public ResponseEntity<Void> discard(@RequestParam("path") String path, Principal principal) {
		mediaService.discard(path, principal.getName());
		return ResponseEntity.noContent().build();
	}

	// 파일 형식·경로가 잘못되면 MediaService가 IllegalArgumentException을 던진다. 400으로 돌려준다.
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<MediaErrorResponse> badRequest(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(new MediaErrorResponse("INVALID_MEDIA", exception.getMessage()));
	}

	public record MediaErrorResponse(String code, String message) {}
}
