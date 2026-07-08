package com.group1.engagement_service.service;

import com.group1.engagement_service.controller.LoyaltyConfigController;
import com.group1.engagement_service.controller.TierBenefitController;
import com.group1.engagement_service.dto.response.LoyaltyConfigResponse;
import com.group1.engagement_service.security.JwtAuthenticationFilter;
import com.group1.engagement_service.security.JwtUtil;
import com.group1.engagement_service.security.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LoyaltyConfigController.class, TierBenefitController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class LoyaltySecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoyaltyConfigService loyaltyConfigService;

    @MockitoBean
    private TierBenefitService tierBenefitService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getConfigRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/loyalty/config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTierBenefitsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/loyalty/tiers/1/benefits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER:READ")
    void getConfigRequiresLoyaltyConfigAuthority() throws Exception {
        mockMvc.perform(get("/loyalty/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "LOYALTY:CONFIG")
    void getConfigAllowsProperAuthority() throws Exception {
        when(loyaltyConfigService.getConfig()).thenReturn(LoyaltyConfigResponse.builder()
                .pointsPerCurrency(new BigDecimal("0.0001"))
                .minOrderAmount(BigDecimal.ZERO)
                .excludedCategories(List.of())
                .expirationMonths(12)
                .evaluationPeriodMonths(12)
                .inheritFromLowerTiers(Boolean.FALSE)
                .tiers(List.of())
                .build());

        mockMvc.perform(get("/loyalty/config"))
                .andExpect(status().isOk());
    }

    @Test
    void getConfigAllowsValidJwtOnLoyaltyRoute() throws Exception {
        Claims claims = new DefaultClaims();
        claims.setSubject("admin");
        claims.put("userId", 1);
        claims.put("email", "admin@gmail.com");
        claims.put("fullName", "System Admin");
        claims.put("authorities", List.of("LOYALTY:CONFIG"));

        when(jwtUtil.extractAllClaims(anyString())).thenReturn(claims);
        when(jwtUtil.extractUserId(claims)).thenReturn(1L);
        when(jwtUtil.extractEmail(claims)).thenReturn("admin@gmail.com");
        when(jwtUtil.extractFullName(claims)).thenReturn("System Admin");
        when(jwtUtil.extractAuthorities(claims)).thenAnswer(invocation -> createAuthorityList("LOYALTY:CONFIG"));
        when(loyaltyConfigService.getConfig()).thenReturn(LoyaltyConfigResponse.builder()
                .pointsPerCurrency(new BigDecimal("0.0001"))
                .minOrderAmount(BigDecimal.ZERO)
                .excludedCategories(List.of())
                .expirationMonths(12)
                .evaluationPeriodMonths(12)
                .inheritFromLowerTiers(Boolean.FALSE)
                .tiers(List.of())
                .build());

        mockMvc.perform(get("/loyalty/config")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "LOYALTY:CONFIG")
    void createBenefitRequiresManageBenefitsAuthority() throws Exception {
        mockMvc.perform(post("/loyalty/tiers/1/benefits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DISCOUNT",
                                  "value": 10,
                                  "description": "10% off"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
