package kr.co.programmers.collabond.api.user.interfaces;

import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.dto.UserUpdateRequestDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserResponseDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserSignUpRequestDto;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping
    public ResponseEntity<UserResponseDto> getMyUserInfo(@AuthenticationPrincipal OAuth2UserInfo userInfo) {
        return ResponseEntity.ok(userService.findByProviderIdToDto(userInfo.getUsername()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PatchMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@AuthenticationPrincipal OAuth2UserInfo userInfo,
                                       @RequestBody UserSignUpRequestDto dto) {
        return ResponseEntity.ok(userService.signup(userInfo.getUsername(), dto));
    }

    @PatchMapping
    public ResponseEntity<UserResponseDto> update(@AuthenticationPrincipal OAuth2UserInfo userInfo,
                                                  @RequestBody UserUpdateRequestDto dto) {
        return ResponseEntity.ok(userService.update(userInfo.getUsername(), dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal OAuth2UserInfo userInfo) {
        userService.delete(userInfo.getUsername());
        return ResponseEntity.ok().build();
    }


}