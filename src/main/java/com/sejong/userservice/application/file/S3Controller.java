package com.sejong.userservice.application.file;

import com.sejong.userservice.application.file.dto.FileUploadRequest;
import com.sejong.userservice.client.file.S3FileUploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "S3 File", description = "파일 업로드 관련 API")
@RestController
@RequestMapping("/api/s3")
@AllArgsConstructor
public class S3Controller {

    private final S3FileUploader s3FileUploader;

    @Operation(summary = "S3 Presigned URL 생성", description = "파일 업로드를 위한 S3 Presigned URL을 생성합니다")
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