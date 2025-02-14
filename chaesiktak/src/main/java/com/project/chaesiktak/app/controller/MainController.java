package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.dto.user.CustomUserDetails;
import com.project.chaesiktak.app.service.NoticeService;
import com.project.chaesiktak.app.service.RecommendRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final NoticeService noticeService;
    private final RecommendRecipeService recommendRecipeService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHome(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 공지사항 가져오기
        List<NoticeDto> notices = noticeService.getLatestNotices();
        List<Map<String, String>> noticeResponse = new ArrayList<>();

        // 제목 및 공지사항 목록 URL
        noticeResponse.add(Map.of(
                "title", "공지사항 목록",
                "url", "/notice/"
        ));

        // 최신 3개의 공지사항 추가
        noticeResponse.addAll(notices.stream()
                .map(notice -> Map.of(
                        "title", notice.getNoticeTitle(),
                        "url", "/notice/" + notice.getId()
                ))
                .collect(Collectors.toList()));

        // 최신 등록 레시피
        List<Map<String, Object>> latestRecipes = recommendRecipeService.getLatestRecipes();

        // 유저 타입별 레시피
        List<Map<String, Object>> userSpecificRecipes = recommendRecipeService.getUserSpecificRecipes(userDetails.getUsername());

        // 응답 데이터 생성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("notices", noticeResponse);
        response.put("latest_recipes", latestRecipes);
        response.put("user_specific_recipes", userSpecificRecipes);

        return ResponseEntity.ok(response);
    }
}
