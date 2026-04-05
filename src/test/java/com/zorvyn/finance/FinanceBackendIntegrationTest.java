package com.zorvyn.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.dto.LoginRequest;
import com.zorvyn.finance.dto.FinancialRecordRequest;
import com.zorvyn.finance.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceBackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String viewerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken("admin@zorvyn.com", "admin123");
        viewerToken = loginAndGetToken("viewer@zorvyn.com", "viewer123");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    @Test
    @DisplayName("Admin can login and get token")
    void adminCanLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@zorvyn.com", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@zorvyn.com", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin can create financial record")
    void adminCanCreateRecord() throws Exception {
        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("5000.00"), TransactionType.INCOME, "Test Category",
                LocalDate.now(), "Test notes");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("Viewer cannot create financial record (403 Forbidden)")
    void viewerCannotCreateRecord() throws Exception {
        FinancialRecordRequest request = new FinancialRecordRequest(
                new BigDecimal("5000.00"), TransactionType.INCOME, "Test Category",
                LocalDate.now(), "Test notes");

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Viewer can view records")
    void viewerCanViewRecords() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Admin can access dashboard summary")
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").exists())
                .andExpect(jsonPath("$.data.totalExpenses").exists())
                .andExpect(jsonPath("$.data.netBalance").exists());
    }

    @Test
    @DisplayName("Unauthenticated request returns 401")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/records"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation fails for invalid record data")
    void validationFailsForInvalidRecord() throws Exception {
        // amount is null - should fail validation
        String invalidJson = "{\"type\":\"INCOME\",\"category\":\"Test\",\"date\":\"2026-01-01\"}";

        mockMvc.perform(post("/api/records")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin can get all users")
    void adminCanGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Viewer cannot access user management (403)")
    void viewerCannotAccessUserManagement() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }
}
