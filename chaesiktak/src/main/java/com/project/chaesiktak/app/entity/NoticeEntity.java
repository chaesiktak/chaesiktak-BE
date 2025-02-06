package com.project.chaesiktak.app.entity;


import com.project.chaesiktak.app.dto.board.NoticeDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// DB의 테이블 역할을 하는 클래스
@Entity
@Getter
@Setter
@Table(name = "notice_table")
public class NoticeEntity extends BaseEntity {
    @Id // pk 컬럼 지정. 필수
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column(length = 20, nullable = false) // 크기 20, not null
    private String noticeWriter;

    @Column
    private String noticeTitle;

    @Column
    private String noticeContent;

    @Column
    private int noticeHits = 0;

    /*
    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentEntity> commentEntityList = new ArrayList<>();
    */

    public static NoticeEntity toSaveEntity(NoticeDto noticeDto) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.setNoticeWriter(noticeDto.getNoticeWriter() != null ? noticeDto.getNoticeWriter() : "관리자");
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContent(noticeDto.getNoticeContent());
        return noticeEntity;
    }

    public static NoticeEntity toUpdateEntity(NoticeDto noticeDto) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.setId(noticeDto.getId());
        noticeEntity.setNoticeWriter(noticeDto.getNoticeWriter());
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContent(noticeDto.getNoticeContent());
        noticeEntity.setNoticeHits(noticeDto.getNoticeHits());
        return noticeEntity;
    }

}

