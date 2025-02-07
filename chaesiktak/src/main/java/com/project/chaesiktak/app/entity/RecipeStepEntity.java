package com.project.chaesiktak.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RecipeStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int step;
    private String description;

    public RecipeStepEntity(int step, String description){
        this.step = step;
        this.description = description;
    }

}