package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.dto.board.IngredientDto;
import com.project.chaesiktak.app.dto.board.NoticeDto;
import com.project.chaesiktak.app.dto.board.RecipeStepDto;
import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.entity.IngredientEntity;
import com.project.chaesiktak.app.entity.NoticeEntity;
import com.project.chaesiktak.app.entity.RecipeStepEntity;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import com.project.chaesiktak.app.repository.RecommendRecipeRepository;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional

public class RecommendRecipeService {
    private final RecommendRecipeRepository recommendRecipeRepository;
    private final UserRepository userRepository;

    @Autowired
    public RecommendRecipeService(RecommendRecipeRepository recommendRecipeRepository,
                                  UserRepository userRepository) {
        this.recommendRecipeRepository = recommendRecipeRepository;
        this.userRepository = userRepository;
    }

    @PersistenceContext
    @Autowired
    private EntityManager entityManager;


    // 레시피 저장
    @PreAuthorize("hasAuthority('ADMIN')")
    public RecommendRecipeDto save(RecommendRecipeDto recommendRecipeDto) {
        // 입력값 검증
        if (recommendRecipeDto == null ||
                isNullOrEmpty(recommendRecipeDto.getTitle()) ||
                isNullOrEmpty(recommendRecipeDto.getSubtext()) ||
                recommendRecipeDto.getKcal() == null ||
                recommendRecipeDto.getTag() == null ||
                isNullOrEmpty(recommendRecipeDto.getPrevtext()) ||
                recommendRecipeDto.getIngredients() == null || recommendRecipeDto.getIngredients().isEmpty() ||
                recommendRecipeDto.getContents() == null || recommendRecipeDto.getContents().isEmpty()) {
            throw new IllegalArgumentException("레시피 제목, 부제목, 칼로리, 태그, 소개글, 재료, 요리 과정은 필수 입력값입니다.");
        }

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

    // 문자열이 null이거나 비어있는지 확인하는 유틸 메서드
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    // 레시피 검색
    /**
     * 레시피 검색 메소드
     *
     * @param query 검색어
     * @param email 사용자 이메일
     * @param type 채식 유형 (문자열)
     * @param includeIngredients 선호하는 재료 목록
     * @param excludeIngredients 비선호하는 재료 목록
     * @return 검색 결과를 담은 Map 리스트
     */
    public List<Map<String, Object>> searchRecipes(String query, String email, String type,
                                                   List<String> includeIngredients, List<String> excludeIngredients) {

        // 1. 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. 검색에 사용할 VeganType 결정
        VeganType searchType = (type != null && !type.trim().isEmpty()) ?
                VeganType.valueOf(type.toUpperCase()) :
                (user.getVeganType() != null ? user.getVeganType() : VeganType.DEFAULT);

        // 3. 태그 기반 레시피 검색 및 키워드 필터링
        List<RecommendRecipeEntity> recipes = recommendRecipeRepository.findByTag(searchType)
                .stream()
                .filter(recipe -> containsKeyword(recipe, query))
                .collect(Collectors.toList());

        // 4. 비선호 재료가 포함된 레시피 제거
        if (excludeIngredients != null && !excludeIngredients.isEmpty()) {
            recipes = recipes.stream()
                    .filter(recipe -> excludeIngredients.stream()
                            .noneMatch(exclude ->
                                    recipe.getIngredients().stream()
                                            .anyMatch(ingredient -> ingredient.getName().toLowerCase().contains(exclude.toLowerCase()))
                            )
                    )
                    .collect(Collectors.toList());
        }

        // 5. 선호 재료 포함 레시피 우선순위 계산 후 내림차순 정렬
        if (includeIngredients != null && !includeIngredients.isEmpty()) {
            recipes.sort(Comparator.comparingInt(recipe -> calculatePriority((RecommendRecipeEntity) recipe, includeIngredients))
                    .reversed());
        }

        // 6. 결과 Map으로 변환
        return recipes.stream()
                .map(recipe -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("recipeTitle", recipe.getTitle());
                    result.put("recipeTag", recipe.getTag().name());
                    result.put("recipeImage", recipe.getImage() != null ? recipe.getImage().toString() : null);
                    result.put("recipePrevtext", recipe.getPrevtext());
                    result.put("url", "/recipe/" + recipe.getId());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 제목, 서브텍스트, 태그에 검색어가 포함되어 있는지 (대소문자 구분 없이) 확인
     *
     * @param recipe 검색 대상 레시피
     * @param query  검색어
     * @return 모든 키워드가 포함되어 있으면 true, 그렇지 않으면 false
     */
    private boolean containsKeyword(RecommendRecipeEntity recipe, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true; // 검색어가 없으면 모든 레시피를 포함
        }
        String[] keywords = query.toLowerCase().split("\\s+");

        String title = recipe.getTitle() != null ? recipe.getTitle().toLowerCase() : "";
        String subtext = recipe.getSubtext() != null ? recipe.getSubtext().toLowerCase() : "";
        String tagStr = recipe.getTag() != null ? recipe.getTag().toString().toLowerCase() : "";

        return Arrays.stream(keywords)
                .allMatch(keyword -> title.contains(keyword)
                        || subtext.contains(keyword)
                        || tagStr.contains(keyword));
    }

    /**
     * 선호 재료 포함 여부에 따른 가중치 계산
     *
     * @param recipe            검색 대상 레시피
     * @param includeIngredients 선호하는 재료 목록
     * @return 포함된 재료 개수를 반환
     */
    private int calculatePriority(RecommendRecipeEntity recipe, List<String> includeIngredients) {
        if (includeIngredients == null || includeIngredients.isEmpty()) {
            return 0;
        }
        return (int) includeIngredients.stream()
                .filter(include ->
                        recipe.getIngredients().stream()
                                .anyMatch(ingredient -> ingredient.getName().toLowerCase().contains(include.toLowerCase()))
                )
                .count();
    }


/*
    // 레시피 조회
    @Transactional
    public RecommendRecipeDto findById(Long id) {
        RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 레시피입니다."));  // 예외 변경
        return convertToDto(recommendRecipeEntity);
    }

 */

    @Transactional
    public ResponseEntity<ApiResponseTemplete<RecommendRecipeDto>> findById(Long id) {
        try {
            RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 레시피입니다."));
            RecommendRecipeDto recommendRecipeDto = convertToDto(recommendRecipeEntity);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_FOUND, recommendRecipeDto);
        } catch (NoSuchElementException e) {
            // 예외 처리: 존재하지 않는 공지 시 ErrorResponse 반환
            return ApiResponseTemplete.error(ErrorCode.RECIPE_NOT_FOUND, null);
        }
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


    public List<Map<String, Object>> getUserSpecificRecipes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<RecommendRecipeEntity> recipeEntities = recommendRecipeRepository.findByTag(user.getVeganType());

        // 원하는 필드만 Map에 담아 반환
        return recipeEntities.stream()
                .map(recipe -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("recipeTitle", recipe.getTitle());
                    map.put("recipeTag", recipe.getTag().toString());
                    map.put("recipeImage", recipe.getImage());
                    map.put("recipePrevtext", recipe.getPrevtext());
                    map.put("url", "/recipe/" + recipe.getId());
                    return map;
                })
                .collect(Collectors.toList());
    }




    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponseTemplete<RecommendRecipeDto>> update(Long id, RecommendRecipeDto recommendRecipeDto) {
        try {
            // 기존 엔티티를 찾음
            RecommendRecipeEntity recommendRecipeEntity = recommendRecipeRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 레시피입니다."));

            // 입력값 검증
            if (recommendRecipeDto == null ||
                    isNullOrEmpty(recommendRecipeDto.getTitle()) ||
                    isNullOrEmpty(recommendRecipeDto.getSubtext()) ||
                    recommendRecipeDto.getKcal() == null ||
                    recommendRecipeDto.getTag() == null ||
                    isNullOrEmpty(recommendRecipeDto.getPrevtext()) ||
                    recommendRecipeDto.getIngredients() == null || recommendRecipeDto.getIngredients().isEmpty() ||
                    recommendRecipeDto.getContents() == null || recommendRecipeDto.getContents().isEmpty()) {
                return ApiResponseTemplete.error(ErrorCode.INVALID_REQUEST, null);
            }

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
                recommendRecipeEntity.getIngredients().removeIf(existingIngredient ->
                        recommendRecipeDto.getIngredients().stream()
                                .noneMatch(ingredientDto -> ingredientDto.getName().equals(existingIngredient.getName()))
                );

                recommendRecipeEntity.setIngredients(recommendRecipeDto.getIngredients().stream()
                        .map(ingredientDto -> new IngredientEntity(ingredientDto.getName(), ingredientDto.getAmount()))
                        .collect(Collectors.toList()));
            }

            // 단계 업데이트 (contents)
            if (recommendRecipeDto.getContents() != null) {
                recommendRecipeEntity.getContents().removeIf(existingStep ->
                        recommendRecipeDto.getContents().stream()
                                .noneMatch(stepDto -> stepDto.getStep() == existingStep.getStep())
                );

                recommendRecipeEntity.setContents(recommendRecipeDto.getContents().stream()
                        .map(stepDto -> new RecipeStepEntity(stepDto.getStep(), stepDto.getDescription()))
                        .collect(Collectors.toList()));
            }

            // 업데이트 후 저장
            recommendRecipeRepository.save(recommendRecipeEntity);

            // 업데이트된 데이터 반환
            RecommendRecipeDto updatedRecipe = convertToDto(recommendRecipeEntity);
            return ApiResponseTemplete.success(SuccessCode.RECIPE_UPDATED, updatedRecipe);

        } catch (NoSuchElementException e) {
            return ApiResponseTemplete.error(ErrorCode.RECIPE_NOT_FOUND, null);
        } catch (Exception e) {
            return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }



    // 레시피 삭제
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        if (!recommendRecipeRepository.existsById(id)){
            throw new NoSuchElementException("존재하지 않는 레시피입니다.");
        }
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

