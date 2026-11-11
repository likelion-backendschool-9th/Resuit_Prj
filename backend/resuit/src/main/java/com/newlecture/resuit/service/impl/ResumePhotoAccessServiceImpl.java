package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.service.ResumePhotoAccessService;
import com.newlecture.resuit.dto.ResumePhotoQuery;
import com.newlecture.resuit.dto.ResumePhotoResource;

import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.PaidFeedbackRequestRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.PostRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResumePhotoAccessServiceImpl implements ResumePhotoAccessService {
    private final CommonResumeRepository common;
    private final TargetResumeRepository target;
    private final PaidFeedbackRequestRepository paid;
    private final UserRepository users;
    private final PostRepository posts;
    private final Path root;

    public ResumePhotoAccessServiceImpl(CommonResumeRepository common, TargetResumeRepository target,
            PaidFeedbackRequestRepository paid, UserRepository users, PostRepository posts,
            @Value("${resuit.media.root:src/main/media}") String root) {
        this.common = common;
        this.target = target;
        this.paid = paid;
        this.users = users;
        this.posts = posts;
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumePhotoResource read(ResumePhotoQuery query) {
        String directory = query.directory();
        String owner = query.owner();
        String filename = query.filename();
        String username = query.username();
        if (!List.of("image", "resumePhotos").contains(directory)
                || !owner.matches("[0-9a-fA-F-]{36}")
                || !filename.matches("[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|gif|webp)")) throw missing();
        UUID ownerId;
        try { ownerId = UUID.fromString(owner); }
        catch (IllegalArgumentException e) { throw missing(); }
        String relative = directory + "/" + owner + "/" + filename;
        var paths = List.of(relative, "/media/" + relative);
        var commonPhotos = common.findByPhotoUrlIn(paths);
        var targetPhotos = target.findByPhotoUrlIn(paths);
        var viewer = username == null ? null : users.findByUsername(username).orElse(null);
        boolean allowed = viewer != null && ownerId.equals(viewer.getId());
        if (!allowed && viewer != null) {
            allowed = commonPhotos.stream().anyMatch(r -> r.getUser().getId().equals(viewer.getId())
                || r.isPublic() || common.hasActiveFeedbackRequest(r.getId())
                || paid.canViewResumePhoto(r.getId(), null, viewer.getId()));
            if (!allowed) allowed = targetPhotos.stream().anyMatch(r -> r.getUser().getId().equals(viewer.getId())
                || target.hasActiveFeedbackRequest(r.getId())
                || paid.canViewResumePhoto(null, r.getId(), viewer.getId()));
        }
        // Deleted resume photos must not become anonymous static resources.
        // Public post/profile images are allowed only when no resume references the file.
        if (!allowed && directory.equals("image") && commonPhotos.isEmpty() && targetPhotos.isEmpty()) {
            allowed = posts.existsByImageUrlInAndUser_IdAndDeletedAtIsNull(paths, ownerId)
                || users.existsByIdAndProfileImageUrlIn(ownerId, paths);
        }
        if (!allowed) throw missing();
        Path file = root.resolve(relative).normalize();
        try {
            // Reject symlinks, including public image links into private snapshots.
            if (!Files.isRegularFile(file) || !file.toRealPath().equals(root.toRealPath().resolve(relative))) throw missing();
        } catch (IOException e) { throw missing(); }
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        String type = switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "image/webp";
        };
        return new ResumePhotoResource(new FileSystemResource(file), type);
    }

    private ResponseStatusException missing() { return new ResponseStatusException(HttpStatus.NOT_FOUND); }
}
