package kr.hhplus.be.server.user.infra.reposistory.port;

import kr.hhplus.be.server.user.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);
    User save(User user);
    List<User> findAll();
    List<User> saveAll(List<User> users);
}