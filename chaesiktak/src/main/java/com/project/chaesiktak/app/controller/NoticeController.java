package com.project.chaesiktak.app.controller;


import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.service.NoticeService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.chaesiktak.global.exception.SuccessCode;
import com.project.chaesiktak.global.exception.ErrorCode;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

    // 공지사항 저장 (POST)
    @PostMapping("/save")
    public ResponseEntity<ApiResponseTemplete<Void>> save(@RequestBody NoticeDto noticeDto) {
        try {
            noticeService.save(noticeDto);
            return ApiResponseTemplete.success(SuccessCode.NOTICE_CREATED, null);
        } catch (IOException e) {
            // IOException 처리
            return ApiResponseTemplete.error(ErrorCode.IO_ERROR, null);
        } catch (Exception e) {
            // 다른 예외 처리
            return ApiResponseTemplete.error(ErrorCode.UNKNOWN_ERROR, null);
        }
    }


    // 공지사항 전체 조회 (GET)
    @GetMapping("/")
    public ResponseEntity<ApiResponseTemplete<List<Map<String, Object>>>> findAll() {
        List<Map<String, Object>> noticeList = noticeService.findAllNotice();
        return ApiResponseTemplete.success(SuccessCode.NOTICE_FOUND, noticeList);
    }



    // 공지사항 상세 조회 (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseTemplete<NoticeDto>> findById(@PathVariable Long id) {
        return noticeService.findById(id); // 그대로 반환
    }


    // 공지사항 수정 (PUT)
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseTemplete<NoticeDto>> update(
            @PathVariable Long id,
            @RequestBody NoticeDto noticeDto) {
        return noticeService.update(id, noticeDto); // 그대로 반환
    }



    // 공지사항 삭제 (DELETE)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseTemplete<Void>> delete(@PathVariable Long id) {
        try {
            // 공지사항 삭제 서비스 호출
            noticeService.delete(id);
            // 성공 메시지 반환
            return ApiResponseTemplete.success(SuccessCode.NOTICE_DELETED, null);
        } catch (NoSuchElementException e) {
            // 존재하지 않는 공지사항 처리
            return ApiResponseTemplete.error(ErrorCode.NOTICE_NOT_FOUND, null);
        } catch (Exception e) {
            // 기타 예외 처리
            return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }

}
