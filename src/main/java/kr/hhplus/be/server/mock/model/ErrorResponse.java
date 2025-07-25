package kr.hhplus.be.server.mock.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 응답 모델")
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드 (문자열)", example = "404")
    private String code;
    @Schema(description = "상세 에러 메시지", example = "User not found.")
    private String message;
}