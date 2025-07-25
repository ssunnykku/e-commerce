package kr.hhplus.be.server.user.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRequest {
    @NotNull
    private long userId;
    @Min(value = 1, message = "충전 금액은 0보다 커야 합니다.")
    @NotNull
    private long amount;
}
