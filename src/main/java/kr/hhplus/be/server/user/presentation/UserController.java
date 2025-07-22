package kr.hhplus.be.server.user.presentation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.user.application.ChargeUseCase;
import kr.hhplus.be.server.user.application.GetUseCase;
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

    @PostMapping("/charge")
     public ResponseEntity<UserResponse> charge(@RequestBody @Valid UserRequest balanceRequest) {
        UserResponse result = chargeUseCase.execute(balanceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getBalance(
            @PathVariable long userId) {
        UserResponse user = getUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }



}
