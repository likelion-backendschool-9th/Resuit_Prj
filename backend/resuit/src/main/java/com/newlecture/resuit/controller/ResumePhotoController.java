package com.newlecture.resuit.controller;

import com.newlecture.resuit.service.ResumePhotoAccessService;
import com.newlecture.resuit.dto.ResumePhotoQuery;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResumePhotoController {
    private final ResumePhotoAccessService photos;

    public ResumePhotoController(ResumePhotoAccessService photos) { this.photos = photos; }

    @GetMapping("/media/{directory:image|resumePhotos}/{owner}/{filename}")
    public ResponseEntity<Resource> read(@PathVariable String directory, @PathVariable String owner,
            @PathVariable String filename, Principal principal) {
        var photo = photos.read(new ResumePhotoQuery(directory, owner, filename,
            principal == null ? null : principal.getName()));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).varyBy("Cookie")
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(photo.contentType())).body(photo.resource());
    }
}
