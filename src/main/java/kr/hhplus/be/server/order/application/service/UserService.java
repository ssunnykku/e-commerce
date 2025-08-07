package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findUser(Long userId) {
        return userRepository.findById(userId);
    }

    public void pay(User user, Long finalPaymentPrice) {
        user.use(finalPaymentPrice);
    }

}
