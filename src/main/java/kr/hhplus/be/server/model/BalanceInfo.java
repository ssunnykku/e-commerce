package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 잔액 정보")
public class BalanceInfo {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    @Schema(description = "현재 잔액", example = "50000")
    private Integer balance;
}