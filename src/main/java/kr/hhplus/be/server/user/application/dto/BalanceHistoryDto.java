package kr.hhplus.be.server.user.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BalanceHistoryDto {
    private long id;
    private long userId;
    private LocalDateTime  created_at;
    private long amount;
    private String type;

}
