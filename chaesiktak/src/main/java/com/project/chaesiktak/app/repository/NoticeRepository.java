package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.entity.NoticeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE NoticeEntity b SET b.noticeHits = b.noticeHits + 1 WHERE b.id = :id")
    void updateHits(@Param("id") Long id);
    //List<NoticeEntity> findTop3ByOrderByIdDesc();
    @Query("SELECT n FROM NoticeEntity n ORDER BY " +
            "CASE WHEN n.updatedTime IS NOT NULL THEN n.updatedTime ELSE n.createdTime END DESC")
    List<NoticeEntity> findTop3ByLatestTime(Pageable pageable);
}
