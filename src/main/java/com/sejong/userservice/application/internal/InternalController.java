package com.sejong.userservice.application.internal;

import com.sejong.userservice.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal", description = "내부 서비스간 통신용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalController {

    private final UserService userService;

    @Operation(summary = "사용자 존재 여부 확인", description = "단일 사용자의 존재 여부를 확인합니다 (내부 API)")
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable("userId") String nickname) {
        boolean exists = userService.exists(nickname);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(summary = "다중 사용자 존재 여부 확인", description = "여러 사용자들이 모두 존재하는지 확인합니다 (내부 API)")
    @PostMapping("/exists")
    public ResponseEntity<Boolean> existAll(@RequestBody List<String> nicknames) {

        boolean existAll = userService.existAll(nicknames);

        return new ResponseEntity<>(existAll, HttpStatus.OK);
    }
}
