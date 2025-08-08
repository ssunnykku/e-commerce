package kr.hhplus.be.server.user.infra.reposistory.adapter;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.UserJpaRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User findById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User save(User user) {
         return userJpaRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public List<User> saveAll(List<User> users) {
        return userJpaRepository.saveAll(users);
    }

    @Override
    public User findByIdRock(Long userId) {
        return userJpaRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(()-> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
