package com.project.chaesiktak.app.service;


import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    public void save(NoticeDto noticeDto) throws IOException{
        NoticeEntity noticeEntity = NoticeEntity.toSaveEntity(noticeDto);
        noticeRepository.save(noticeEntity);
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
    public NoticeDto findById(Long id) {
        NoticeEntity noticeEntity = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));
        noticeRepository.updateHits(id);
        return NoticeDto.toNoticeDto(noticeEntity);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public NoticeDto update(Long id, NoticeDto noticeDto) {
        // 기존 엔티티를 찾음
        NoticeEntity noticeEntity = noticeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다."));

        // 제목과 내용만 업데이트 (id는 변경 X)
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContent(noticeDto.getNoticeContent());

        // 업데이트 후 저장
        noticeRepository.save(noticeEntity);

        return findById(id); // 업데이트된 데이터를 반환
    }




    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id){
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

        // 🔹 DTO 변환 (NoticeDto 수정 없이 해결)
        return notices.stream()
                .map(n -> {
                    NoticeDto dto = new NoticeDto();
                    BeanUtils.copyProperties(n, dto); // ✅ 자동 매핑
                    return dto;
                })
                .collect(Collectors.toList());
    }



}
