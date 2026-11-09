package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.MediaUploadResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

	MediaUploadResponse upload(MultipartFile file, String username);

	void discard(String path, String username);

	String finalizeForPost(String path, UUID userId, String mediaType);
    String prepareResumePhoto(String path, UUID userId);
    String copyResumePhoto(String path, UUID userId);
    String preserveResumePhoto(String path, UUID userId);
    String restoreResumePhoto(String key, UUID userId);
    byte[] readSnapshotPhoto(String key);
    void deleteSnapshotPhoto(String key);
}
