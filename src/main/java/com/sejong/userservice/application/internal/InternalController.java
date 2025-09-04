package com.sejong.userservice.application.internal;

import com.sejong.userservice.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existAll(@RequestBody List<String> nicknames) {

        boolean existAll = userService.existAll(nicknames);

        return new ResponseEntity<>(existAll, HttpStatus.OK);
    }

    @Operation(summary = "방장 및 협력자 존재 여부 확인", description = "방장 및 협력자의 존재 여부 확인 (내부 API)")
    @GetMapping("/{username}/exists/multiple")
    public ResponseEntity<Boolean> exists(@PathVariable("username") String username,
                                          @RequestParam("collaboratorUsernames") List<String> collaboratorUsernames) {
        boolean exists = userService.exists(username, collaboratorUsernames);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(summary ="usernames을 받아서 실제 nicknames를 반환", description = "usernames을 받아서 실제 nicknames를 반환")
    @GetMapping("/all")
    public ResponseEntity<Map<String,String>> getAllUsernames(@RequestParam("usernames") List<String> usernames){
        Map<String,String> usernamesMap = userService.getAllUsernames(usernames);
        return new ResponseEntity<>(usernamesMap, HttpStatus.OK);
    }


}
