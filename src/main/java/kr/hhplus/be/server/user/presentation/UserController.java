package kr.hhplus.be.server.user.presentation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.user.application.useCase.ChargeUseCase;
import kr.hhplus.be.server.user.application.useCase.ConsumeBalanceUseCase;
import kr.hhplus.be.server.user.application.useCase.GetUseCase;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/balance")
public class UserController {
    private final ChargeUseCase chargeUseCase;
    private final GetUseCase getUseCase;
    private final ConsumeBalanceUseCase consumeBalanceUseCase;


    @PostMapping("/charge")
     public ResponseEntity<UserResponse> charge(@RequestBody @Valid UserRequest request) {
        UserResponse result = chargeUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getBalance(
            @PathVariable long userId) {
        UserResponse user = getUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PostMapping("/use")
    public ResponseEntity<UserResponse> consume(@RequestBody @Valid UserRequest request) {
        UserResponse result = consumeBalanceUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
