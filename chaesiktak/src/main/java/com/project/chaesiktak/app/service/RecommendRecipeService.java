package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.dto.board.IngredientDto;
import com.project.chaesiktak.app.dto.board.RecipeStepDto;
import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.entity.IngredientEntity;
import com.project.chaesiktak.app.entity.RecipeStepEntity;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import com.project.chaesiktak.app.repository.RecommendRecipeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Transactional
    public RecommendRecipeDto findById(Long id) {
        RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 레시피입니다."));  // 예외 변경
        return convertToDto(recommendRecipeEntity);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLatestRecipes() {
        List<RecommendRecipeEntity> recipeEntities = recommendRecipeRepository.findLatest9Recipes();

        return recipeEntities.stream().map(recipe -> {
            Map<String, Object> response = new HashMap<>();
            response.put("recipeTitle", recipe.getTitle());
            response.put("recipeTag", recipe.getTag());
            response.put("recipePrevtext", recipe.getPrevtext());
            response.put("recipeImage", recipe.getImage());
            response.put("url", "/recipe/" + recipe.getId());  // 상세 페이지 URL 추가
            return response;
        }).collect(Collectors.toList());
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

/*
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
    }*/

    @PersistenceContext
    private EntityManager entityManager;

    @PreAuthorize("hasAuthority('ADMIN')")
    public RecommendRecipeDto update(Long id, RecommendRecipeDto recommendRecipeDto) {
        // 기존 엔티티를 찾음
        RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));

        // 제목, 서브텍스트, 칼로리, 태그, 이전 설명, 즐겨찾기 필드 업데이트
        recommendRecipeEntity.setTitle(recommendRecipeDto.getTitle());
        recommendRecipeEntity.setSubtext(recommendRecipeDto.getSubtext());
        recommendRecipeEntity.setKcal(recommendRecipeDto.getKcal());
        recommendRecipeEntity.setTag(recommendRecipeDto.getTag());
        recommendRecipeEntity.setPrevtext(recommendRecipeDto.getPrevtext());
        recommendRecipeEntity.setFavorite(recommendRecipeDto.isFavorite());

        // 이미지 필드 업데이트
        if (recommendRecipeDto.getImage() != null) {
            recommendRecipeEntity.setImage(recommendRecipeDto.getImage());
        }

        // 재료 업데이트 (ingredients)
        if (recommendRecipeDto.getIngredients() != null) {
            // 기존 재료에서 더 이상 존재하지 않는 항목 삭제
            for (IngredientEntity existingIngredient : recommendRecipeEntity.getIngredients()) {
                if (!recommendRecipeDto.getIngredients().stream()
                        .anyMatch(ingredientDto -> ingredientDto.getName().equals(existingIngredient.getName()))) {
                    // ingredients에서 더 이상 존재하지 않는 항목 삭제
                    entityManager.remove(existingIngredient);
                }
            }

            // 새로운 재료 리스트 설정
            recommendRecipeEntity.setIngredients(recommendRecipeDto.getIngredients().stream()
                    .map(ingredientDto -> new IngredientEntity(ingredientDto.getName(), ingredientDto.getAmount()))
                    .collect(Collectors.toList()));
        }

        // 단계 업데이트 (contents)
        if (recommendRecipeDto.getContents() != null) {
            // 기존 단계에서 더 이상 존재하지 않는 항목 삭제
            for (RecipeStepEntity existingStep : recommendRecipeEntity.getContents()) {
                if (!recommendRecipeDto.getContents().stream()
                        .anyMatch(stepDto -> stepDto.getStep() == existingStep.getStep())) {
                    // contents에서 더 이상 존재하지 않는 항목 삭제
                    entityManager.remove(existingStep);
                }
            }

            // 새로운 단계 리스트 설정
            recommendRecipeEntity.setContents(recommendRecipeDto.getContents().stream()
                    .map(stepDto -> new RecipeStepEntity(stepDto.getStep(), stepDto.getDescription()))
                    .collect(Collectors.toList()));
        }

        // 업데이트 후 저장
        recommendRecipeRepository.save(recommendRecipeEntity);

        // 업데이트된 데이터를 반환
        return convertToDto(recommendRecipeEntity);
    }




    // 레시피 삭제
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        recommendRecipeRepository.deleteById(id);
    }

    private RecommendRecipeDto convertToDto(RecommendRecipeEntity recommendRecipeEntity) {
        // 이미지 ID (Integer)를 경로로 변환
        Integer imageId = recommendRecipeEntity.getImage();
        // String imagePath = (imageId != null) ? "https://example.com/images/" + imageId + ".jpg" : "default_image_path.jpg";

        // Ingredients List 변환
        List<IngredientDto> ingredientDtos = recommendRecipeEntity.getIngredients() != null ?
                recommendRecipeEntity.getIngredients().stream()
                        .map(ingredientEntity -> new IngredientDto(ingredientEntity.getName(), ingredientEntity.getAmount()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        // Recipe Steps List 변환
        List<RecipeStepDto> recipeStepDtos = recommendRecipeEntity.getContents() != null ?
                recommendRecipeEntity.getContents().stream()
                        .map(recipeStepEntity -> new RecipeStepDto(recipeStepEntity.getStep(), recipeStepEntity.getDescription()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        return new RecommendRecipeDto(
                recommendRecipeEntity.getId(),
                imageId, // 이미지 경로 설정
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

