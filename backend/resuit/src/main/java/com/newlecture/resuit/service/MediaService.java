package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.newlecture.resuit.dto.MediaUploadResponse;

public interface MediaService {

	MediaUploadResponse upload(MultipartFile file, String username);

	void discard(String path, String username);

	String finalizeForPost(String path, UUID userId, String mediaType);
}
