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
}
