package com.sejong.userservice.application.file;

import com.sejong.userservice.application.file.dto.FileUploadRequest;
import com.sejong.userservice.application.file.dto.PreSignedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final FileUploader fileUploader;

    @PostMapping("/presigned-url")
    public ResponseEntity<PreSignedUrl> generatePresignedUrl(@RequestBody FileUploadRequest fileUploadRequest) {
        PreSignedUrl preSignedUrl = fileUploader.generatePreSignedUrl(
                fileUploadRequest.fileName(),
                fileUploadRequest.contentType(),
                fileUploadRequest.fileType()
        );
        return ResponseEntity.ok(preSignedUrl);
    }

}