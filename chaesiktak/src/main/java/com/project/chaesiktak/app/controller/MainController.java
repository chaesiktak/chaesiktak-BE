package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.repository.NoticeRepository;
import com.project.chaesiktak.app.service.NoticeService;
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
    private final NoticeService noticeService;

    public MainController(NoticeRepository noticeRepository, NoticeService noticeService) {
        this.noticeRepository = noticeRepository;
        this.noticeService = noticeService;
    }

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

}
