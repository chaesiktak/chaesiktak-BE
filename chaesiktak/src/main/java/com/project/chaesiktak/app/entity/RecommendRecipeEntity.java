package com.project.chaesiktak.app.entity;

import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class RecommendRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer image;
    private String title;
    private String subtext;
    private String kcal;
    private String tag;
    private String prevtext;
    private boolean isFavorite;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<IngredientEntity> ingredients;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<RecipeStepEntity> contents;

}