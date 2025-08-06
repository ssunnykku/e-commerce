package kr.hhplus.be.server.order.application.domainService;

import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public User findUser(Long userId) {
        return userRepository.findById(userId);
    }

    public void pay(User user, Long finalPaymentPrice) {
        user.use(finalPaymentPrice);
    }

}
