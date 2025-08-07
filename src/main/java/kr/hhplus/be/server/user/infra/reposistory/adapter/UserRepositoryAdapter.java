package kr.hhplus.be.server.user.infra.reposistory.adapter;

import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.UserJpaRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId);
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
}
