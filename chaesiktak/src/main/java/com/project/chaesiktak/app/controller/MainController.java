package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.repository.NoticeRepository;
import com.project.chaesiktak.app.repository.RecommendRecipeRepository;
import com.project.chaesiktak.app.service.NoticeService;
import com.project.chaesiktak.app.service.RecommendRecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MainController {
    private final NoticeService noticeService;
    private final RecommendRecipeService recommendRecipeService;

    public MainController(NoticeService noticeService, RecommendRecipeService recommendRecipeService) {
        this.noticeService = noticeService;
        this.recommendRecipeService = recommendRecipeService;
    }
/*
    @GetMapping("/")
    public ResponseEntity<List<Map<String, String>>> getLatestNotices() {
        List<NoticeDto> notices = noticeService.getLatestNotices();  // noticeService 인스턴스를 통해 호출
        List<Map<String, String>> response = new ArrayList<>();

        // 제목 및 공지사항 목록 URL 추가 (가장 먼저)
        response.add(Map.of(
                "title", "공지사항 목록",
                "url", "/notice/"
        ));

        // 최신 3개의 공지사항 추가
        response.addAll(notices.stream()
                .map(notice -> Map.of(
                        "title", notice.getNoticeTitle(),
                        "url", "/notice/" + notice.getId()
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
*/
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHome() {
        // 공지사항 가져오기
        List<NoticeDto> notices = noticeService.getLatestNotices();
        List<Map<String, String>> noticeResponse = new ArrayList<>();

        // 제목 및 공지사항 목록 URL 추가 (가장 먼저)
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

        // 레시피 가져오기
        List<Map<String, Object>> recipeResponse = recommendRecipeService.getLatestRecipes();

        // 두 개의 데이터 묶기
        Map<String, Object> response = new HashMap<>();
        response.put("notices", noticeResponse);
        response.put("latest_recipes", recipeResponse);

        return ResponseEntity.ok(response);
    }


}
