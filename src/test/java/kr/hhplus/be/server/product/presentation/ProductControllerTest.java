package kr.hhplus.be.server.product.presentation;

import kr.hhplus.be.server.common.exception.ApiControllerAdvice;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductListUseCase;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetProductUseCase getProductUseCase;

    @Mock
    private GetProductListUseCase getProductListUseCase;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
    }

    @Nested
    @DisplayName("GET /products")
    class GetProductList {

        @Test
        @DisplayName("상품 리스트 조회 성공 시 200과 JSON 반환")
        void getProductListSuccess() throws Exception {
            // given
            List<ProductResponse> products = List.of(
                    new ProductResponse(1L, "상품A", 1000L, 10L),
                    new ProductResponse(2L, "상품B", 2000L, 20L)
            );
            given(getProductListUseCase.execute()).willReturn(products);

            // when & then
            mockMvc.perform(get("/products")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(products.size()))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("상품A"))
                    .andExpect(jsonPath("$[0].price").value(1000))
                    .andExpect(jsonPath("$[0].stock").value(10));
        }
    }

    @Nested
    @DisplayName("GET /products/{id}")
    class GetProductDetails {

        @Test
        @DisplayName("존재하는 상품 ID로 조회 성공 시 200과 상품 정보 반환")
        void getProductDetailsSuccess() throws Exception {
            // given
            ProductResponse product = new ProductResponse(1L, "상품A", 1000L, 10L);
            given(getProductUseCase.execute(anyLong())).willReturn(product);

            // when & then
            mockMvc.perform(get("/products/{id}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("상품A"))
                    .andExpect(jsonPath("$.price").value(1000))
                    .andExpect(jsonPath("$.stock").value(10));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID 조회 시 404 반환 (예외처리 필요)")
        void getProductDetailsNotFound() throws Exception {
            // given
            given(getProductUseCase.execute(anyLong()))
                    .willThrow(new OutOfStockListException(ErrorCode.NOT_FOUND_ENTITY, List.of(1L)));

            // when & then
            mockMvc.perform(get("/products/{id}", 99999L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(ErrorCode.NOT_FOUND_ENTITY.getStatus().value()))
                    .andExpect(jsonPath("$.code").value(String.valueOf(ErrorCode.NOT_FOUND_ENTITY.getStatus().value())))
                    .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_ENTITY.getMessage()));
        }
    }
}
