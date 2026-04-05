package com.finance;

import com.finance.dto.auth.LoginRequest;
import com.finance.dto.auth.RegisterRequest;
import com.finance.dto.auth.AuthResponse;
import com.finance.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testRegister() throws Exception {
        RegisterRequest req = new RegisterRequest("New User", "newuser@finance.dev", "password123");
        AuthResponse resp = new AuthResponse("dummy-token", "Bearer", 4L, "New User", "newuser@finance.dev", null);
        when(authService.register(any())).thenReturn(resp);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("dummy-token"));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin@finance.dev", "password123");
        AuthResponse resp = new AuthResponse("admin-token", "Bearer", 1L, "Admin User", "admin@finance.dev", null);
        when(authService.login(any())).thenReturn(resp);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-token"));
    }
}
