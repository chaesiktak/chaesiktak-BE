package com.project.chaesiktak.app.dto.board;

import com.project.chaesiktak.app.domain.VeganType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class RecommendRecipeDto {
    private Long id;
    private Integer image;
    private String title;
    private String subtext;
    private String kcal;
    private VeganType tag;
    private String prevtext;
    private boolean isFavorite;
    private List<IngredientDto> ingredients;
    private List<RecipeStepDto> contents;
}
