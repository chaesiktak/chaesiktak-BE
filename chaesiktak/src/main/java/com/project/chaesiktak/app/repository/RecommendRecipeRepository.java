package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendRecipeRepository extends JpaRepository<RecommendRecipeEntity, Long> {
}