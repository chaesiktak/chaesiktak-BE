package com.project.chaesiktak.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class IngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String amount;

    public IngredientEntity(String name, String amount) {
        this.name = name;
        this.amount = amount;
    }

}