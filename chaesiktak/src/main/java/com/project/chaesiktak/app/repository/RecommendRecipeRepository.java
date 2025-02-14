package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



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

    // 엔티티의 프로퍼티 이름이 'tag'라면, 메소드 이름도 findByTag로 변경
    List<RecommendRecipeEntity> findByTag(VeganType tag);

    // title, subtext, tag를 포함하여 검색
    @Query("SELECT r FROM RecommendRecipeEntity r " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.subtext) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.tag) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<RecommendRecipeEntity> searchRecipes(@Param("searchTerm") String searchTerm);

}


