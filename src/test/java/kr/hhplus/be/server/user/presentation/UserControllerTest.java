package kr.hhplus.be.server.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.useCase.ChargeUseCase;
import kr.hhplus.be.server.user.application.useCase.GetUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChargeUseCase chargeUseCase;

    @Mock
    private GetUserUseCase getUserUseCase;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void charge() throws Exception {
        UserRequest request = new UserRequest(1L,  10000L);
        UserResponse response = new UserResponse(1L, "testUser", 20000L);

        when(chargeUseCase.execute(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.name").value("testUser"))
                .andExpect(jsonPath("$.balance").value(20000L));
    }

    @Test
    void getBalance() throws Exception {
        long userId = 1L;
        UserResponse response = new UserResponse(userId, "testUser", 15000L);

        when(getUserUseCase.execute(userId)).thenReturn(response);

        mockMvc.perform(get("/balance/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("testUser"))
                .andExpect(jsonPath("$.balance").value(15000L));
    }

}