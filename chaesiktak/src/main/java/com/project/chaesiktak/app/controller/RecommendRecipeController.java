package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.dto.user.CustomUserDetails;
import com.project.chaesiktak.app.service.RecommendRecipeService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipe")
public class RecommendRecipeController {

    private final RecommendRecipeService recommendRecipeService;
    // 레시피 저장 (POST)
    @PostMapping("/save")
    public ResponseEntity<ApiResponseTemplete<Void>> save(@RequestBody RecommendRecipeDto recommendRecipeDto) {
        try {
            recommendRecipeService.save(recommendRecipeDto);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_CREATED, null);
        } catch (Exception e) {
            // 모든 예외 처리
            return ApiResponseTemplete.error(ErrorCode.UNKNOWN_ERROR, null);
        }
    }
    // 레시피 전체 조회 (GET)
    @GetMapping("/")
    public ResponseEntity<ApiResponseTemplete<List<Map<String, Object>>>> findAll() {
        List<Map<String, Object>> recipelist = recommendRecipeService.findAllRecipe();
        return ApiResponseTemplete.success(SuccessCode.RECIPE_FOUND, recipelist);
    }
    // 레시피 상세 조회 (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseTemplete<RecommendRecipeDto>> findById(@PathVariable Long id) {
        try {
            RecommendRecipeDto recipeDto = recommendRecipeService.findById(id);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_FOUND, recipeDto);
        } catch (IllegalArgumentException e) {  // 예외 처리 수정
            return ApiResponseTemplete.error(ErrorCode.RECIPE_NOT_FOUND, null);
        }
    }
    // 레시피 수정 (PUT)
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseTemplete<RecommendRecipeDto>> update(@PathVariable Long id, @RequestBody RecommendRecipeDto recipeDto) {
        try {
            RecommendRecipeDto updatedRecipe = recommendRecipeService.update(id, recipeDto);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_UPDATED, updatedRecipe);
        } catch (NoSuchElementException e) {
            return ApiResponseTemplete.error(ErrorCode.RECIPE_NOT_FOUND, null);
        }
    }
    // 레시피 삭제 (DELETE)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseTemplete<Void>> delete(@PathVariable Long id) {
        try {
            recommendRecipeService.delete(id);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_DELETED, null);
        } catch (NoSuchElementException e) {
            return ApiResponseTemplete.error(ErrorCode.RECIPE_NOT_FOUND, null);
        }
    }
    // 레시피 검색 (GET)
    @GetMapping("/search")
    public ResponseEntity<?> searchRecipes(
            @RequestParam String query,  // 검색어
            @RequestParam(required = false) String type, // 선택적 검색 타입
            @RequestParam(required = false) List<String> includeIngredients, // 선호 재료
            @RequestParam(required = false) List<String> excludeIngredients, // 비선호 재료
            @AuthenticationPrincipal CustomUserDetails userDetails) { // 로그인한 사용자 정보

        String email = userDetails.getUsername(); // 로그인한 사용자의 이메일
        List<Map<String, Object>> results = recommendRecipeService.searchRecipes(query, email, type, includeIngredients, excludeIngredients);

        return ResponseEntity.ok(ApiResponseTemplete.success(SuccessCode.RECIPE_FOUND, results));
    }
}
