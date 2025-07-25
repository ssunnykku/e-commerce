package kr.hhplus.be.server.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
   private long userId;
   private String name;
   private long balance;
}
