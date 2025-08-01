package kr.hhplus.be.server.user.application.dto;

import kr.hhplus.be.server.user.domain.entity.User;

public record UserResponse (long userId, String name, long balance) {
   public static UserResponse from(User user) {
      return new UserResponse(user.getUserId(), user.getName(), user.getBalance());
   }
}
