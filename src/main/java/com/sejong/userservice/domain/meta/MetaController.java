package com.sejong.userservice.domain.meta;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @Operation(summary = "유저수, 게시글 수 조회 api")
    @GetMapping("/count")
    public ResponseEntity<MetaCountResponse> metaResponse() {

        MetaCountResponse response = metaService.getMetaCountInfo();
        return ResponseEntity.status(200)
                .body(response);
    }

    @Operation(summary = "어드민 데시보드 프로젝트, CS, 뉴스, 카테고리 수 반환 api")
    @GetMapping("/admin/count")
    public ResponseEntity<MetaCountAdminResponse> metaAdminResponse() {

        MetaCountAdminResponse response = metaService.getMetaAdminCountInfo();
        return ResponseEntity.status(200)
                .body(response);
    }
}
