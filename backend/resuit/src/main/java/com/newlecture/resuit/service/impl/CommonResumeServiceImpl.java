package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.CommonResumeResponse;
import com.newlecture.resuit.dto.CreateCommonResumeRequest;
import com.newlecture.resuit.dto.UpdateCommonResumeRequest;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommonResumeServiceImpl implements CommonResumeService {
    private final CommonResumeRepository resumes;
    private final UserRepository users;

    @Override
    @Transactional(readOnly = true)
    public List<CommonResumeResponse> listMine(UUID userId) {
        return resumes.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResumeResponse getMine(UUID userId, Long resumeId) {
        return toResponse(findOwned(userId, resumeId));
    }

    @Override
    @Transactional
    public CommonResumeResponse create(UUID userId, CreateCommonResumeRequest request) {
        User user = users.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        String title = requireTitle(request.title());
        String field = normalizeField(request.field());
        Map<String, Object> content = request.content() == null ? Map.of() : request.content();
        CommonResume saved = resumes.save(CommonResume.builder()
                .user(user)
                .title(title)
                .field(field)
                .content(content)
                .isPublic(false)
                .build());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CommonResumeResponse update(UUID userId, Long resumeId, UpdateCommonResumeRequest request) {
        CommonResume resume = findOwned(userId, resumeId);
        if (request.title() != null) {
            resume.rename(requireTitle(request.title()));
        }
        if (request.field() != null) {
            resume.updateField(normalizeField(request.field()));
        }
        if (request.content() != null) {
            resume.updateContent(request.content());
        }
        if (request.isPublic() != null) {
            resume.setPublic(request.isPublic());
            if (!request.isPublic()) {
                unpinIfNeeded(userId, resumeId);
            }
        }
        return toResponse(resume);
    }

    @Override
    @Transactional
    public void delete(UUID userId, Long resumeId) {
        CommonResume resume = findOwned(userId, resumeId);
        unpinIfNeeded(userId, resumeId);
        try {
            resumes.delete(resume);
            resumes.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "지원·피드백·특화 이력서에 연결된 공통 이력서는 삭제할 수 없습니다.");
        }
    }

    private void unpinIfNeeded(UUID userId, Long resumeId) {
        users.findById(userId).ifPresent(user -> {
            if (user.getPinnedCommonResume() != null
                    && resumeId.equals(user.getPinnedCommonResume().getId())) {
                user.pinResume(null);
            }
        });
    }

    private CommonResume findOwned(UUID userId, Long resumeId) {
        return resumes.findByIdAndUserId(resumeId, userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "공통 이력서를 찾을 수 없습니다."));
    }

    private String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이력서 이름이 필요합니다.");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이력서 이름은 100자 이하여야 합니다.");
        }
        return trimmed;
    }

    private String normalizeField(String field) {
        if (field == null || field.isBlank()) return null;
        String normalized = field.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("FRONTEND") && !normalized.equals("BACKEND")) {
            // 화면 한글 값도 허용
            if ("프론트엔드".equals(field.trim())) return "FRONTEND";
            if ("백엔드".equals(field.trim())) return "BACKEND";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "분야는 FRONTEND 또는 BACKEND여야 합니다.");
        }
        return normalized;
    }

    private CommonResumeResponse toResponse(CommonResume resume) {
        return new CommonResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getField(),
                resume.isPublic(),
                resume.getContent(),
                resume.getCreatedAt(),
                resume.getUpdatedAt());
    }
}
