package com.project.chaesiktak.app.entity;

import com.project.chaesiktak.app.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "USER_FAVORITE_RECIPES")
public class UserFavoriteRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecommendRecipeEntity recipe;

    @Builder
    public UserFavoriteRecipeEntity(User user, RecommendRecipeEntity recipe) {
        this.user = user;
        this.recipe = recipe;
    }
}
