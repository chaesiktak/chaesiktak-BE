package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.repository.NoticeRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;


@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
 
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponseTemplete<NoticeDto>> save(NoticeDto noticeDto) throws IOException {
        // 입력값 검증
        if (noticeDto == null ||
                isNullOrEmpty(noticeDto.getNoticeTitle()) ||
                isNullOrEmpty(noticeDto.getNoticeContent())) {
            return ApiResponseTemplete.error(ErrorCode.INVALID_REQUEST, null);
        }

        NoticeEntity noticeEntity = NoticeEntity.toSaveEntity(noticeDto);
        noticeRepository.save(noticeEntity);

        return ApiResponseTemplete.success(SuccessCode.NOTICE_CREATED, noticeDto);
    }

    public List<NoticeDto> findAll(){
        List<NoticeEntity> noticeEntityList = noticeRepository.findAll();
        List<NoticeDto> noticeDtoList = new ArrayList<>();
        for(NoticeEntity noticeEntity: noticeEntityList){
            noticeDtoList.add(NoticeDto.toNoticeDto(noticeEntity));
        }
        return noticeDtoList;
    }

    @Transactional
    public void updateHits(Long id) {
        noticeRepository.updateHits(id);
    }

    @Transactional
    public ResponseEntity<ApiResponseTemplete<NoticeDto>> findById(Long id) {
        try {
            NoticeEntity noticeEntity = noticeRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 공지입니다."));
            noticeRepository.updateHits(id);
            NoticeDto noticeDto = NoticeDto.toNoticeDto(noticeEntity);
            return ApiResponseTemplete.success(SuccessCode.NOTICE_FOUND, noticeDto);
        } catch (NoSuchElementException e) {
            // 예외 처리: 존재하지 않는 공지 시 ErrorResponse 반환
            return ApiResponseTemplete.error(ErrorCode.NOTICE_NOT_FOUND, null);
        }
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponseTemplete<NoticeDto>> update(Long id, NoticeDto noticeDto) {
        try {
            // 기존 엔티티 조회
            NoticeEntity noticeEntity = noticeRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다."));

            // 제목과 내용 검증
            if (isNullOrEmpty(noticeDto.getNoticeTitle()) || isNullOrEmpty(noticeDto.getNoticeContent())) {
                return ApiResponseTemplete.error(ErrorCode.INVALID_REQUEST, null);
            }

            // 제목과 내용 업데이트
            noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
            noticeEntity.setNoticeContent(noticeDto.getNoticeContent());

            // 업데이트 후 저장
            noticeRepository.save(noticeEntity);

            // 업데이트된 데이터 반환
            NoticeDto updatedNotice = NoticeDto.toNoticeDto(noticeEntity);
            return ApiResponseTemplete.success(SuccessCode.NOTICE_UPDATED, updatedNotice);

        } catch (NoSuchElementException e) {
            return ApiResponseTemplete.error(ErrorCode.NOTICE_NOT_FOUND, null);
        } catch (Exception e) {
            return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
      


    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        // 공지사항 존재 여부 확인
        if (!noticeRepository.existsById(id)) {
            throw new NoSuchElementException("존재하지 않는 공지입니다.");
        }

        // 공지사항 삭제
        noticeRepository.deleteById(id);
    }

    public List<Map<String, Object>> findAllNotice() {
        List<NoticeEntity> noticeEntities = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return noticeEntities.stream().map(notice -> {
            Map<String, Object> response = new HashMap<>();
            response.put("noticeWriter", notice.getNoticeWriter());
            response.put("noticeTitle", notice.getNoticeTitle());
            response.put("noticeHits", notice.getNoticeHits());
            response.put("noticeTime",
                    notice.getUpdatedTime() != null ? notice.getUpdatedTime() : notice.getCreatedTime()
            );
            response.put("url", "/notice/" + notice.getId()); // 상세 페이지 URL 추가
            return response;
        }).collect(Collectors.toList());
    }

    public List<NoticeDto> getLatestNotices() {
        Pageable topThree = PageRequest.of(0, 3);
        List<NoticeEntity> notices = noticeRepository.findTop3ByLatestTime(topThree);

        return notices.stream()
                .map(n -> {
                    NoticeDto dto = new NoticeDto();
                    BeanUtils.copyProperties(n, dto); // 자동 매핑
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
