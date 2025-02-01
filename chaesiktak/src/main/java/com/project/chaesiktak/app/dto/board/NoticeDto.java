package com.project.chaesiktak.app.dto.board;


import com.project.chaesiktak.app.entity.NoticeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    private Long id;
    private String noticeWriter;
    private String noticeTitle;
    private String noticeContent;
    private int noticeHits;
    private LocalDateTime noticeCreatedTime;
    private LocalDateTime noticeUpdatedTime;

    public NoticeDto(Long id, String noticeWriter, String noticeTitle, String noticeContent, int noticeHits, LocalDateTime noticeCreatedTime) {
        this.id = id;
        this.noticeWriter = noticeWriter;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeHits = noticeHits;
        this.noticeCreatedTime = noticeCreatedTime;
    }

    public static NoticeDto toNoticeDto(NoticeEntity noticeEntity) {
        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setId(noticeEntity.getId());
        noticeDto.setNoticeWriter(noticeEntity.getNoticeWriter());
        noticeDto.setNoticeTitle(noticeEntity.getNoticeTitle());
        noticeDto.setNoticeContent(noticeEntity.getNoticeContent());
        noticeDto.setNoticeHits(noticeEntity.getNoticeHits());
        noticeDto.setNoticeCreatedTime(noticeEntity.getCreatedTime());
        noticeDto.setNoticeUpdatedTime(noticeEntity.getUpdatedTime());
        return noticeDto;
    }
}
