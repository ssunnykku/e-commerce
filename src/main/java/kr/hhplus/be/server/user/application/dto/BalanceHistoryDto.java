package kr.hhplus.be.server.user.application.dto;

import java.time.LocalDateTime;


public record BalanceHistoryDto(long id, long userId, LocalDateTime created_at, long amount, String type) {
}


