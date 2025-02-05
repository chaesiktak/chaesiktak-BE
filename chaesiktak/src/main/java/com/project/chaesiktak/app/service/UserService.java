package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
