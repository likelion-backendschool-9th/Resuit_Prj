package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CommonResumeResponse;
import com.newlecture.resuit.dto.CreateCommonResumeRequest;
import com.newlecture.resuit.dto.UpdateCommonResumeRequest;
import java.util.List;
import java.util.UUID;

public interface CommonResumeService {
    List<CommonResumeResponse> listMine(UUID userId);
    CommonResumeResponse getMine(UUID userId, Long resumeId);
    CommonResumeResponse create(UUID userId, CreateCommonResumeRequest request);
    CommonResumeResponse update(UUID userId, Long resumeId, UpdateCommonResumeRequest request);
    void delete(UUID userId, Long resumeId);
}
