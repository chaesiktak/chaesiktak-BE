package com.project.chaesiktak.app.dto.board;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class RecommendRecipeDto {
    private Long id;
    private String image;
    private String title;
    private String subtext;
    private String kcal;
    private String tag;
    private String prevtext;
    private boolean isFavorite;
    private List<IngredientDto> ingredients;
    private List<RecipeStepDto> contents;

}