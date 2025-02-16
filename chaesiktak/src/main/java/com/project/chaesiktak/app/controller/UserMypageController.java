package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.dto.user.UserUpdateVeganDto;
import com.project.chaesiktak.app.dto.user.UserUpdateNameDto;
import com.project.chaesiktak.app.dto.user.UserUpdateNicknameDto;
import com.project.chaesiktak.app.dto.user.UserWithdrawalDto;
import com.project.chaesiktak.app.service.UserService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify/mypage")
@RequiredArgsConstructor
public class UserMypageController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * 유저 비건 타입 변경
     */
    @PatchMapping("/vegan")
    public ResponseEntity<ApiResponseTemplete<String>> updateVeganType(
            HttpServletRequest request,
            @RequestBody UserUpdateVeganDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateVeganType(email, dto.getVeganType());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("비건 타입이 변경되었습니다.")
                            .data(dto.getVeganType().name())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("비건 타입 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * 유저 이름 변경
     */
    @PatchMapping("/name")
    public ResponseEntity<ApiResponseTemplete<String>> updateUserName(
            HttpServletRequest request,
            @RequestBody UserUpdateNameDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateUserName(email, dto.getUserName());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("이름이 변경되었습니다.")
                            .data(dto.getUserName())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("이름 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * 유저 닉네임 변경
     */
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponseTemplete<String>> updateUserNickname(
            HttpServletRequest request,
            @RequestBody UserUpdateNicknameDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateUserNickname(email, dto.getUserNickName());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("닉네임이 변경되었습니다.")
                            .data(dto.getUserNickName())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("닉네임 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponseTemplete<String>> withdrawUser(
            HttpServletRequest request,
            @RequestBody UserWithdrawalDto dto){

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }
        // 회원 탈퇴 로직 실행
        userService.withdrawUser(email, dto.getReason());

        return ResponseEntity.ok(
                ApiResponseTemplete.<String>builder()
                        .status(200)
                        .success(true)
                        .message("회원 탈퇴가 완료되었습니다.")
                        .data("탈퇴 사유: " + dto.getReason())
                        .build()
        );
    }
    /**
     * 레시피 좋아요 추가
     */
    @PostMapping("/favorite/{recipeId}")
    public ResponseEntity<ApiResponseTemplete<String>> likeRecipe(
            HttpServletRequest request,
            @PathVariable Long recipeId) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean liked = userService.likeRecipe(email, recipeId);

        if (liked) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("레시피 좋아요 성공")
                            .data(null)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("이미 좋아요한 레시피입니다.")
                            .data(null)
                            .build()
            );
        }
    }
    /**
     * 레시피 좋아요 취소
     */
    @DeleteMapping("/favorite/{recipeId}")
    public ResponseEntity<ApiResponseTemplete<String>> unlikeRecipe(
            HttpServletRequest request,
            @PathVariable Long recipeId) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean unliked = userService.unlikeRecipe(email, recipeId);

        if (unliked) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("레시피 좋아요 취소 성공")
                            .data(null)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("좋아요한 기록이 없는 레시피입니다.")
                            .data(null)
                            .build()
            );
        }
    }
    /**
     * 사용자가 좋아요한 레시피 목록 조회
     */
    @GetMapping("/favorite")
    public ResponseEntity<ApiResponseTemplete<List<RecommendRecipeDto>>> getFavoriteRecipes(HttpServletRequest request) {
        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        List<RecommendRecipeDto> favoriteRecipes = userService.getFavoriteRecipes(email);

        return ResponseEntity.ok(
                ApiResponseTemplete.<List<RecommendRecipeDto>>builder()
                        .status(200)
                        .success(true)
                        .message("좋아요한 레시피 목록")
                        .data(favoriteRecipes)
                        .build()
        );
    }
}
