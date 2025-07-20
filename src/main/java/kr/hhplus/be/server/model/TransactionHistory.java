package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "잔액 거래 내역")
public class TransactionHistory {
    @Schema(description = "거래 ID", example = "1")
    private Long transactionId;
    @Schema(description = "거래 유형 (CHARGE 또는 PURCHASE)", example = "CHARGE")
    private String type;
    @Schema(description = "거래 금액 (PURCHASE의 경우 음수)", example = "100000")
    private Integer amount;
    @Schema(description = "거래 일시", example = "2025-07-01 10:00:00")
    private LocalDateTime date;
}