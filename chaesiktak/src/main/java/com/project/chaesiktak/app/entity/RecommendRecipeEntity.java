package com.project.chaesiktak.app.entity;

import com.project.chaesiktak.app.domain.VeganType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor(force = true)
@Entity

public class RecommendRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    private String title;

    private String subtext;

    private String kcal;

    @Enumerated(EnumType.STRING)
    private VeganType tag; // 레시피의 비건 타입

    private String prevtext;
    private boolean isFavorite;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "recipe_id")
    @BatchSize(size = 10)  // 한 번에 10개씩 조회
    private List<IngredientEntity> ingredients;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "recipe_id")
    @BatchSize(size = 10)  // 한 번에 10개씩 조회
    private List<RecipeStepEntity> contents;
}
