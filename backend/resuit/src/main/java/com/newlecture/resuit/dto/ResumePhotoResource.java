package com.newlecture.resuit.dto;

import org.springframework.core.io.Resource;

/** Authorized binary photo resource, written as an image by the controller. */
public record ResumePhotoResource(Resource resource, String contentType) {}
