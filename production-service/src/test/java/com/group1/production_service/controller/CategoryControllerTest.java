package com.group1.production_service.controller;

import com.group1.production_service.exception.GlobalExceptionHandler;
import com.group1.production_service.security.JwtAuthenticationFilter;
import com.group1.production_service.security.JwtUtil;
import com.group1.production_service.security.SecurityConfig;
import com.group1.production_service.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(authorities = "CATEGORY:READ")
    void getCategoriesAcceptsLiteralNullParentFilter() throws Exception {
        when(categoryService.getCategories(null, Boolean.TRUE)).thenReturn(List.of());

        mockMvc.perform(get("/categories")
                        .param("parent_id", "null")
                        .param("is_active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categories fetched"));

        verify(categoryService).getCategories(null, Boolean.TRUE);
    }

    @Test
    @WithMockUser(authorities = "CATEGORY:READ")
    void getCategoriesRejectsInvalidParentFilter() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("parent_id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("parent_id must be a number or null"));
    }
}
