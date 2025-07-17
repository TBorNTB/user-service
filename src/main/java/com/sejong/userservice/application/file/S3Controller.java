package com.sejong.userservice.application.file;

import com.sejong.userservice.application.file.dto.FileUploadRequest;
import com.sejong.userservice.infrastructure.file.S3FileUploader;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@AllArgsConstructor
public class S3Controller {

    private final S3FileUploader s3FileUploader;

    @PostMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(@RequestBody FileUploadRequest fileUploadRequest) {
        String presignedUrl = s3FileUploader.generatePreSignedUrl(
                fileUploadRequest.fileName(),
                fileUploadRequest.contentType(),
                fileUploadRequest.fileType()
        ).uploadUrl();
        return ResponseEntity.ok(presignedUrl);
    }

}