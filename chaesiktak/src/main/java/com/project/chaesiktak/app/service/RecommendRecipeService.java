package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.dto.board.IngredientDto;
import com.project.chaesiktak.app.dto.board.RecipeStepDto;
import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.entity.IngredientEntity;
import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.entity.RecipeStepEntity;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import com.project.chaesiktak.app.repository.RecommendRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendRecipeService {
    private final RecommendRecipeRepository recommendRecipeRepository;

    // 레시피 저장
    @PreAuthorize("hasAuthority('ADMIN')")
    public RecommendRecipeDto save(RecommendRecipeDto recommendRecipeDto) {
        RecommendRecipeEntity recommendRecipeEntity = new RecommendRecipeEntity();
        BeanUtils.copyProperties(recommendRecipeDto, recommendRecipeEntity); // DTO -> Entity 변환

        // IngredientEntity 및 RecipeStepEntity 추가
        List<IngredientEntity> ingredients = recommendRecipeDto.getIngredients().stream()
                .map(ingredientDto -> new IngredientEntity(ingredientDto.getName(), ingredientDto.getAmount()))
                .collect(Collectors.toList());
        recommendRecipeEntity.setIngredients(ingredients);

        List<RecipeStepEntity> recipeSteps = recommendRecipeDto.getContents().stream()
                .map(stepDto -> new RecipeStepEntity(stepDto.getStep(), stepDto.getDescription()))
                .collect(Collectors.toList());
        recommendRecipeEntity.setContents(recipeSteps);

        recommendRecipeRepository.save(recommendRecipeEntity);

        return convertToDto(recommendRecipeEntity); // 저장 후 DTO 반환
    }

    // 레시피 조회
    public RecommendRecipeDto findById(Long id) {
        RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));
        return convertToDto(recommendRecipeEntity);
    }

    // 레시피 목록 조회
    public List<Map<String, Object>> findAllRecipe() {
        List<RecommendRecipeEntity> recipeEntities = recommendRecipeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return recipeEntities.stream().map(recipe -> {
            Map<String, Object> response = new HashMap<>();
            response.put("recipeTitle", recipe.getTitle());
            response.put("recipeTag", recipe.getTag());
            response.put("recipePrevtext", recipe.getPrevtext());
            response.put("recipeImage", recipe.getImage());
            response.put("url", "/recipe/" + recipe.getId()); // 상세 페이지 URL 추가
            return response;
        }).collect(Collectors.toList());
    }


    // 레시피 수정
    @PreAuthorize("hasAuthority('ADMIN')")
    public RecommendRecipeDto update(Long id, RecommendRecipeDto recommendRecipeDto) {
        RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));

        recommendRecipeEntity.setTitle(recommendRecipeDto.getTitle());
        recommendRecipeEntity.setSubtext(recommendRecipeDto.getSubtext());
        recommendRecipeEntity.setKcal(recommendRecipeDto.getKcal());
        recommendRecipeEntity.setTag(recommendRecipeDto.getTag());
        recommendRecipeEntity.setPrevtext(recommendRecipeDto.getPrevtext());
        recommendRecipeEntity.setFavorite(recommendRecipeDto.isFavorite());

        // 재료 및 단계 수정
        recommendRecipeEntity.setIngredients(recommendRecipeDto.getIngredients().stream()
                .map(ingredientDto -> new IngredientEntity(ingredientDto.getName(), ingredientDto.getAmount()))
                .collect(Collectors.toList()));

        recommendRecipeEntity.setContents(recommendRecipeDto.getContents().stream()
                .map(stepDto -> new RecipeStepEntity(stepDto.getStep(), stepDto.getDescription()))
                .collect(Collectors.toList()));

        recommendRecipeRepository.save(recommendRecipeEntity);

        return convertToDto(recommendRecipeEntity);
    }

    // 레시피 삭제
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        recommendRecipeRepository.deleteById(id);
    }

    // DTO로 변환하는 메서드
    private RecommendRecipeDto convertToDto(RecommendRecipeEntity recommendRecipeEntity) {
        List<IngredientDto> ingredientDtos = recommendRecipeEntity.getIngredients().stream()
                .map(ingredientEntity -> new IngredientDto(ingredientEntity.getName(), ingredientEntity.getAmount()))
                .collect(Collectors.toList());

        List<RecipeStepDto> recipeStepDtos = recommendRecipeEntity.getContents().stream()
                .map(recipeStepEntity -> new RecipeStepDto(recipeStepEntity.getStep(), recipeStepEntity.getDescription()))
                .collect(Collectors.toList());

        return new RecommendRecipeDto(
                recommendRecipeEntity.getId(),
                "image_path", // 이미지 URL 또는 경로 추가 (필요시)
                recommendRecipeEntity.getTitle(),
                recommendRecipeEntity.getSubtext(),
                recommendRecipeEntity.getKcal(),
                recommendRecipeEntity.getTag(),
                recommendRecipeEntity.getPrevtext(),
                recommendRecipeEntity.isFavorite(),
                ingredientDtos,
                recipeStepDtos
        );
    }
}

