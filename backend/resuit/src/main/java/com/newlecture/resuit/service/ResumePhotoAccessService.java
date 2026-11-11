package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.ResumePhotoQuery;
import com.newlecture.resuit.dto.ResumePhotoResource;

public interface ResumePhotoAccessService {
    ResumePhotoResource read(ResumePhotoQuery query);
}
