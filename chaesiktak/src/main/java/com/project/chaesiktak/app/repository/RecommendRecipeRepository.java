package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/*
@Repository
public interface RecommendRecipeRepository extends JpaRepository<RecommendRecipeEntity, Long> {

}*/


@Repository
public interface RecommendRecipeRepository extends JpaRepository<RecommendRecipeEntity, Long> {

    @EntityGraph(attributePaths = {"ingredients", "contents"})  // Fetch Join 효과
    Optional<RecommendRecipeEntity> findById(Long id);


    @Query("SELECT r FROM RecommendRecipeEntity r " +
            "LEFT JOIN FETCH r.ingredients " +
            "WHERE r.id = :id")
    Optional<RecommendRecipeEntity> findByIdWithIngredients(@Param("id") Long id);

    @Query("SELECT r FROM RecommendRecipeEntity r " +
            "LEFT JOIN FETCH r.contents " +
            "WHERE r.id = :id")
    Optional<RecommendRecipeEntity> findByIdWithContents(@Param("id") Long id);

    // 최신순 9개 조회
    @Query("SELECT r FROM RecommendRecipeEntity r ORDER BY r.id DESC")
    List<RecommendRecipeEntity> findTop9Recipes(Pageable pageable);

    // 기본 조회 메서드
    default List<RecommendRecipeEntity> findLatest9Recipes() {
        return findTop9Recipes(PageRequest.of(0, 9));
    }

}


