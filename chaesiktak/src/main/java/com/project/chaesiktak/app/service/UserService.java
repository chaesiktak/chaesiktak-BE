package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.dto.board.RecommendRecipeDto;
import com.project.chaesiktak.app.entity.RecommendRecipeEntity;
import com.project.chaesiktak.app.entity.UserFavoriteRecipeEntity;
import com.project.chaesiktak.app.entity.WithdrawalEntity;
import com.project.chaesiktak.app.repository.RecommendRecipeRepository;
import com.project.chaesiktak.app.repository.UserFavoriteRecipeRepository;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.app.repository.WithdrawalRepository;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final UserFavoriteRecipeRepository userFavoriteRecipeRepository;
    private final RecommendRecipeRepository recommendRecipeRepository;

    @Transactional
    public boolean updateVeganType(String email, VeganType veganType) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVeganType(veganType);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserName(String email, String userName) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserName(userName);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserNickname(String email, String userNickName) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserNickName(userNickName);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void withdrawUser(String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION, "유저를 찾을 수 없습니다."));

        WithdrawalEntity withdrawal = WithdrawalEntity.builder()
                .email(user.getEmail())
                .reason(reason)
                .build();
        withdrawalRepository.save(withdrawal);

        userRepository.delete(user);
    }

    @Transactional
    public boolean likeRecipe(String email, Long recipeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendRecipeEntity recipe = recommendRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다."));

        // 이미 좋아요한 레시피인지 확인
        Optional<UserFavoriteRecipeEntity> existingFavorite = userFavoriteRecipeRepository.findByUserAndRecipe(user, recipe);
        if (existingFavorite.isPresent()) {
            return false; // 이미 좋아요한 경우 처리 안 함
        }

        UserFavoriteRecipeEntity favoriteRecipe = new UserFavoriteRecipeEntity(user, recipe);
        userFavoriteRecipeRepository.save(favoriteRecipe);

        return true;
    }
    /**
     * 레시피 좋아요 취소
     */
    @Transactional
    public boolean unlikeRecipe(String email, Long recipeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendRecipeEntity recipe = recommendRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다."));

        Optional<UserFavoriteRecipeEntity> favoriteRecipe = userFavoriteRecipeRepository.findByUserAndRecipe(user, recipe);

        if (favoriteRecipe.isPresent()) {
            userFavoriteRecipeRepository.delete(favoriteRecipe.get());
            return true;
        }

        return false; // 좋아요가 되어 있지 않은 상태에서 취소 요청한 경우
    }
    /**
     * 사용자가 좋아요한 레시피 조회
     */
    @Transactional(readOnly = true)
    public List<RecommendRecipeDto> getFavoriteRecipes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<UserFavoriteRecipeEntity> favoriteRecipes = userFavoriteRecipeRepository.findByUser(user);

        return favoriteRecipes.stream()
                .map(fav -> new RecommendRecipeDto(
                        fav.getRecipe().getId(),
                        fav.getRecipe().getImage(),
                        fav.getRecipe().getTitle(),
                        fav.getRecipe().getSubtext(),
                        fav.getRecipe().getKcal(),
                        fav.getRecipe().getTag(),
                        fav.getRecipe().getPrevtext(),
                        true, // 사용자가 좋아요한 레시피이므로 true
                        null, // 재료 및 단계는 필요 없을 경우 null
                        null
                ))
                .collect(Collectors.toList());
    }
}
