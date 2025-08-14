package kr.hhplus.be.server.user.infra.reposistory.port;

import kr.hhplus.be.server.user.domain.entity.User;

import java.util.List;

public interface UserRepository {
    User findById(Long userId);
    User save(User user);
    List<User> findAll();
    List<User> saveAll(List<User> users);
}