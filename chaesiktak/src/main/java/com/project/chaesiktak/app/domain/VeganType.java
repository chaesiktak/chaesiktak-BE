package com.project.chaesiktak.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VeganType {
    DEFAULT("DEFAULT","아직 VTYPE을 설정하지 않은 기본 타입 사용자 입니다."),
    FRUITERIAN("Fruitarian", "과일-곡식은 섭취하지만 채소, 육류 등은 섭취하지 않음"),
    VEGAN("Vegan", "동물성 제품을 포함한 모든 육류와 유제품을 섭취하지 않음"),
    LACTO("Lacto", "채식주의자이지만 유제품은 섭취"),
    OVO("Ovo", "채식주의자이지만 달걀은 섭취"),
    LACTO_OVO("Lacto-Ovo", "채식주의자이지만 유제품과 달걀은 섭취"),
    PESCO("Pesco", "채식주의자이지만 해산물은 섭취"),
    POLLO("Pollo", "채식주의자이지만 닭고기는 섭취"),
    FLEXITARIAN("Flexitarian", "상황에 따라 유연하게 육식을 허용");

    private final String name; // 영문명
    private final String description; // 설명문구
}
