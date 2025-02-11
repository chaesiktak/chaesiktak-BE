package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.entity.UserFavoriteRecipeEntity;
import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRecipeRepository extends JpaRepository<UserFavoriteRecipeEntity, Long> {
    List<UserFavoriteRecipeEntity> findByUser(User user);
    Optional<UserFavoriteRecipeEntity> findByUserAndRecipe(User user, RecommendRecipeEntity recipe);
}
