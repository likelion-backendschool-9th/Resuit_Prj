package com.newlecture.resuit.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;

/** Resolves editable resume photos; MediaService owns validation, file copies and rollback cleanup. */
@Component
public class ResumePhotoResolver {
    private final MediaService mediaService;

    public ResumePhotoResolver(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // The caller skips this method for an omitted PATCH field. Blank input removes the photo.
    public String resolve(UUID userId, String raw) {
        try {
            return mediaService.prepareResumePhoto(raw == null ? null : raw.trim(), userId);
        } catch (IllegalArgumentException exception) {
            throw new InvalidResumeException("사진을 저장하지 못했어요. 본인이 올린 사진인지 확인하고 다시 올려 주세요.");
        }
    }
}
