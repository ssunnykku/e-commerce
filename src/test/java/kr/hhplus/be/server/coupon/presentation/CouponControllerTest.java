package kr.hhplus.be.server.coupon.presentation;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateCouponUseCase createCouponUseCase;

    @InjectMocks
    private CouponController couponController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(couponController)
                .build();
    }

    @Nested
    @DisplayName("POST /coupons")
    class IssueCoupon {

        @Test
        @DisplayName("정상 발급 시 201 CREATED 반환")
        void issueCouponSuccess() throws Exception {
            // given
            CouponRequest request = new CouponRequest(1L, 10L);
            CouponResponse response = new CouponResponse(1L, 10L, 10L, null, null, false);

            Mockito.when(createCouponUseCase.execute(any(CouponRequest.class)))
                    .thenReturn(response);

            // when & then
            mockMvc.perform(post("/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.couponTypeId").value(10))
                    .andExpect(jsonPath("$.isUsed").value(false));
        }

        @Test
        @DisplayName("필드 누락 시 400 BAD_REQUEST 반환")
        void issueCouponValidationError() throws Exception {
            // 누락된 필드
            String invalidJson = "{ 'userId': null, 'couponTypeId': null }";

            mockMvc.perform(post("/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("JSON 형식 오류 시 400 BAD_REQUEST 반환")
        void issueCouponInvalidJson() throws Exception {
            String invalidJson = "{ 'userId': 'string', 'couponTypeId': 10 }";

            mockMvc.perform(post("/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }
}
