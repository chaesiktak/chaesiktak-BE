package com.project.chaesiktak.app.service;


import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    @PreAuthorize("hasRole('ADMIN')")
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

    @Transactional(readOnly = true)
    public NoticeDto findById(Long id) {
        NoticeEntity noticeEntity = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));
        return NoticeDto.toNoticeDto(noticeEntity);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public NoticeDto update(NoticeDto noticeDto){
        NoticeEntity noticeEntity = NoticeEntity.toUpdateEntity(noticeDto);
        noticeRepository.save(noticeEntity);
        return findById(noticeDto.getId());
    }


    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id){
        noticeRepository.deleteById(id);
    }

    public Page<NoticeDto> paging(Pageable pageable){
        int page = pageable.getPageNumber()-1;
        int pageLimit = 10; //1쪽당 보여지는 개수
        Page<NoticeEntity> noticeEntities = noticeRepository.findAll(
                PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id"))
        );
        return noticeEntities.map(NoticeDto::toNoticeDto);
    }

}
