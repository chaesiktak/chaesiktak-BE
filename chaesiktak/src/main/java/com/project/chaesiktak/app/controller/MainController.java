package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.repository.NoticeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MainController {
    private final NoticeRepository noticeRepository;

    public MainController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Map<String, String>>> getLatestNotices() {
        List<NoticeEntity> notices = noticeRepository.findTop3ByOrderByIdDesc();
        List<Map<String, String>> response = new ArrayList<>();

        // 제목 및 공지사항 목록 URL 추가 (가장 먼저)
        response.add(Map.of(
                "title", "공지사항 목록",
                "url", "/notice/"
        ));

        //최신 3개의 공지사항 추가
        response.addAll(notices.stream()
                .map(notice -> Map.of(
                        "title", notice.getNoticeTitle(),
                        "url", "/notice/" + notice.getId()
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}
