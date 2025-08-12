package kr.hhplus.be.server.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderRequest.OrderItemRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.application.useCase.OrderUseCase;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Nested
    @DisplayName("POST /orders")
    class PlaceProductOrder {

        @Test
        @DisplayName("정상 주문 시 200 OK 반환")
        void placeOrderSuccess() throws Exception {
            // given
            long discountAmount = 1000L;
            long userId = 1L;

            OrderRequest request = OrderRequest.of(
                    userId,
                    100L,
                    discountAmount,
                    List.of(OrderItemRequest.of(10L,2))
            );

            OrderResponse response = new OrderResponse(1234L, userId);

            Mockito.when(orderUseCase.execute(any(OrderRequest.class))).thenReturn(response);

            // when & then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1234L));
        }

        @Test
        @DisplayName("필드 누락 시 400 BAD_REQUEST 반환")
        void placeOrderValidationError() throws Exception {
            String invalidJson = "{ 'userId': null, 'couponId': null, 'orderItems': null }";

            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("JSON 형식 오류 시 400 BAD_REQUEST 반환")
        void placeOrderInvalidJson() throws Exception {
            String invalidJson = "{ 'userId': 'string', 'couponId': 100, 'orderItems': [] }";

            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }
}

