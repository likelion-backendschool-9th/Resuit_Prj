package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.MediaUploadResponse;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.MediaService;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaServiceImpl implements MediaService {

	private static final String IMAGE = "IMAGE";
	private static final String VIDEO = "VIDEO";

	private final UserRepository userRepository;
	private final Path mediaRoot;

	public MediaServiceImpl(
			UserRepository userRepository,
			@Value("${resuit.media.root:src/main/media}") String mediaRoot) {
		this.userRepository = userRepository;
		this.mediaRoot = Path.of(mediaRoot).toAbsolutePath().normalize();
	}

	@Override
	public MediaUploadResponse upload(MultipartFile file, String username) {
		var user = findUser(username);
		var mediaType = mediaType(file);
		var directory = mediaType.equals(IMAGE) ? "tmpImage" : "tmpVideo";
		var extension = extension(file.getOriginalFilename(), mediaType);
		var filename = UUID.randomUUID() + extension;
		var relativePath = Path.of(directory, user.getId().toString(), filename);
		var target = resolveInsideMediaRoot(relativePath);

		try {
			Files.createDirectories(target.getParent());
			file.transferTo(target);
		} catch (IOException | IllegalStateException exception) {
			throw new IllegalArgumentException("미디어 파일을 저장할 수 없습니다.", exception);
		}

		return new MediaUploadResponse(mediaType, relativePath.toString().replace('\\', '/'));
	}

	@Override
	public String finalizeForPost(String path, UUID userId, String expectedMediaType) {
		if (path == null || path.isBlank()) {
			return null;
		}

		var sourceRelativePath = parseRelativePath(path);
		var temporaryDirectory = expectedMediaType.equals(IMAGE) ? "tmpImage" : "tmpVideo";
		if (sourceRelativePath.getNameCount() != 3
				|| !sourceRelativePath.getName(0).toString().equals(temporaryDirectory)
				|| !sourceRelativePath.getName(1).toString().equals(userId.toString())) {
			throw new IllegalArgumentException("현재 사용자가 업로드한 임시 미디어만 게시할 수 있습니다.");
		}

		var source = resolveInsideMediaRoot(sourceRelativePath);
		var publishedDirectory = expectedMediaType.equals(IMAGE) ? "image" : "video";
		var publishedRelativePath = Path.of(publishedDirectory, userId.toString(), sourceRelativePath.getFileName().toString());
		var target = resolveInsideMediaRoot(publishedRelativePath);

		try {
			if (!Files.isRegularFile(source)) {
				throw new IllegalArgumentException("임시 미디어 파일을 찾을 수 없습니다.");
			}
			Files.createDirectories(target.getParent());
			try {
				Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(source, target);
			}
		} catch (IOException exception) {
			throw new IllegalArgumentException("미디어 파일을 게시할 수 없습니다.", exception);
		}

		return publishedRelativePath.toString().replace('\\', '/');
	}

	@Override
	public void discard(String path, String username) {
		var user = findUser(username);
		var relativePath = parseRelativePath(path);
		if (relativePath.getNameCount() != 3
				|| (!relativePath.getName(0).toString().equals("tmpImage")
						&& !relativePath.getName(0).toString().equals("tmpVideo"))
				|| !relativePath.getName(1).toString().equals(user.getId().toString())) {
			throw new IllegalArgumentException("현재 사용자가 업로드한 임시 미디어만 삭제할 수 있습니다.");
		}

		try {
			Files.deleteIfExists(resolveInsideMediaRoot(relativePath));
		} catch (IOException exception) {
			throw new IllegalArgumentException("임시 미디어를 삭제할 수 없습니다.", exception);
		}
	}

    // Snapshot files have no static resource mapping. Only authenticated snapshot endpoints read them.
    @Override
    public String prepareResumePhoto(String path, UUID userId) {
        if (path == null || path.isBlank()) return null;
        var relative = ownedPhoto(path, userId);
        if (!relative.getName(0).toString().equals("resumePhotos")) return copyResumePhoto(path, userId);
        return "/media/" + relative.toString().replace('\\', '/');
    }

    @Override
    public String copyResumePhoto(String path, UUID userId) {
        if (path == null || path.isBlank()) return null;
        return copyPhoto(resolveInsideMediaRoot(ownedPhoto(path, userId)), userId, false);
    }

    @Override
    public String preserveResumePhoto(String path, UUID userId) {
        if (path == null || path.isBlank()) return null;
        return copyPhoto(resolveInsideMediaRoot(ownedPhoto(path, userId)), userId, true);
    }

    @Override
    public String restoreResumePhoto(String key, UUID userId) {
        return key == null ? null : copyPhoto(snapshotPath(key), userId, false);
    }

    private Path ownedPhoto(String path, UUID userId) {
        if (path.length() > 500) throw new IllegalArgumentException("사진 경로는 500자 이하여야 합니다.");
        var relative = parseRelativePath(path.startsWith("/media/") ? path.substring(7) : path);
        if (relative.getNameCount() != 3
                || !Set.of("image", "tmpImage", "resumePhotos").contains(relative.getName(0).toString())
                || !relative.getName(1).toString().equals(userId.toString())) {
            throw new IllegalArgumentException("본인이 업로드한 사진만 사용할 수 있습니다.");
        }
        var resolved = resolveInsideMediaRoot(relative);
        try {
            if (!Files.isRegularFile(resolved) || !resolved.toRealPath().startsWith(mediaRoot.toRealPath()))
                throw new IllegalArgumentException("사진 파일을 찾을 수 없습니다.");
        } catch (IOException e) { throw new IllegalArgumentException("사진 파일을 읽을 수 없습니다.", e); }
        extension(relative.getFileName().toString(), IMAGE);
        return relative;
    }

    private String copyPhoto(Path source, UUID userId, boolean snapshot) {
        var name = UUID.randomUUID() + extension(source.getFileName().toString(), IMAGE);
        var relative = snapshot ? Path.of("resumeSnapshots", name) : Path.of("resumePhotos", userId.toString(), name);
        var target = resolveInsideMediaRoot(relative);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
        } catch (IOException e) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) { }
            throw new IllegalArgumentException("제출 사진을 복사할 수 없습니다.", e);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            try { Files.deleteIfExists(target); }
                            catch (IOException e) { LoggerFactory.getLogger(MediaServiceImpl.class).error("사진 복사 롤백 실패: {}", target, e); }
                        }
                    }
                });
        }
        return snapshot ? name : "/media/" + relative.toString().replace('\\', '/');
    }

    private Path snapshotPath(String key) {
        if (key == null || !key.matches("[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|gif|webp)"))
            throw new IllegalArgumentException("유효하지 않은 제출 사진 키입니다.");
        return resolveInsideMediaRoot(Path.of("resumeSnapshots", key));
    }

    @Override
    public byte[] readSnapshotPhoto(String key) {
        try { return Files.readAllBytes(snapshotPath(key)); }
        catch (IOException e) { throw new IllegalArgumentException("제출 사진을 읽을 수 없습니다.", e); }
    }

    @Override
    public void deleteSnapshotPhoto(String key) {
        if (key == null) return;
        try { Files.deleteIfExists(snapshotPath(key)); }
        catch (IOException e) { throw new IllegalStateException("제출 사진 삭제에 실패했습니다. 다음 정리 작업에서 재시도합니다.", e); }
    }

    private User findUser(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
	}

	private String mediaType(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}
		var contentType = file.getContentType();
		if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			return IMAGE;
		}
		if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("video/")) {
			return VIDEO;
		}
		throw new IllegalArgumentException("이미지 또는 영상 파일만 업로드할 수 있습니다.");
	}

	private String extension(String originalFilename, String mediaType) {
		if (originalFilename == null || originalFilename.isBlank()) {
			throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
		}
		String filename;
		try {
			filename = Path.of(originalFilename).getFileName().toString();
		} catch (InvalidPathException exception) {
			throw new IllegalArgumentException("유효하지 않은 파일명입니다.", exception);
		}
		var dot = filename.lastIndexOf('.');
		if (dot < 1 || dot == filename.length() - 1) {
			throw new IllegalArgumentException("파일 확장자가 필요합니다.");
		}
		var extension = filename.substring(dot).toLowerCase(Locale.ROOT);
		var validExtensions = mediaType.equals(IMAGE)
				? Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp")
				: Set.of(".mp4", ".webm", ".mov", ".m4v");
		if (!validExtensions.contains(extension)) {
			throw new IllegalArgumentException("파일 형식과 확장자가 일치하지 않습니다.");
		}
		return extension;
	}

	private Path parseRelativePath(String path) {
		try {
			var relativePath = Path.of(path).normalize();
			if (relativePath.isAbsolute() || relativePath.startsWith("..")) {
				throw new IllegalArgumentException("유효하지 않은 미디어 경로입니다.");
			}
			return relativePath;
		} catch (InvalidPathException exception) {
			throw new IllegalArgumentException("유효하지 않은 미디어 경로입니다.", exception);
		}
	}

	private Path resolveInsideMediaRoot(Path relativePath) {
		var resolved = mediaRoot.resolve(relativePath).normalize();
		if (!resolved.startsWith(mediaRoot)) {
			throw new IllegalArgumentException("유효하지 않은 미디어 경로입니다.");
		}
		return resolved;
	}
}
