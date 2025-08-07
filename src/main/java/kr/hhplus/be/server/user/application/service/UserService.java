package kr.hhplus.be.server.user.application.service;

import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User charge(Long userId, Long amount) {
        User user = userRepository.findById(userId);
        user.increaseBalance(amount);
        return user;
    }
}
