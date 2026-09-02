package com.zest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import com.zest.dto.item.ItemResponse;
import com.zest.dto.product.CreateProductRequest;
import com.zest.dto.product.ProductResponse;
import com.zest.dto.product.UpdateProductRequest;
import com.zest.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getAllProducts_shouldReturn200() throws Exception {

        ProductResponse product =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "user1",
                        LocalDateTime.now(),
                        null,
                        null
                );

        Page<ProductResponse> page =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.getAllProducts(0, 10))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());

        verify(productService)
                .getAllProducts(0, 10);
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getProductById_shouldReturn200() throws Exception {

        ProductResponse product =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "user1",
                        LocalDateTime.now(),
                        null,
                        null
                );

        when(productService.getProductById(1L))
                .thenReturn(product);

        mockMvc.perform(
                get("/api/v1/products/1")
        )
        .andExpect(status().isOk());

        verify(productService)
                .getProductById(1L);
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void createProduct_shouldReturn201() throws Exception {

        CreateProductRequest request =
                new CreateProductRequest("Laptop");

        ProductResponse response =
                new ProductResponse(
                        1L,
                        "Laptop",
                        "user1",
                        LocalDateTime.now(),
                        null,
                        null
                );

        when(productService.createProduct(
                any(CreateProductRequest.class),
                eq("user1")
        )).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated());

        verify(productService)
                .createProduct(
                        any(CreateProductRequest.class),
                        eq("user1")
                );
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void updateProduct_shouldReturn200() throws Exception {

        UpdateProductRequest request =
                new UpdateProductRequest("Gaming Laptop");

        ProductResponse response =
                new ProductResponse(
                        1L,
                        "Gaming Laptop",
                        "user1",
                        LocalDateTime.now(),
                        "user1",
                        LocalDateTime.now()
                );

        when(productService.updateProduct(
                eq(1L),
                any(UpdateProductRequest.class),
                eq("user1")
        )).thenReturn(response);

        mockMvc.perform(
                put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk());

        verify(productService)
                .updateProduct(
                        eq(1L),
                        any(UpdateProductRequest.class),
                        eq("user1")
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteProductAsAdmin_shouldReturn204() throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .delete("/api/v1/products/1")
        )
        .andExpect(status().isNoContent());

        verify(productService)
                .deleteProduct(1L);
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void deleteProductAsUser_shouldReturn403() throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .delete("/api/v1/products/1")
        )
        .andExpect(status().isForbidden());

        verify(productService, never())
                .deleteProduct(anyLong());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getItemsByProductId_shouldReturn200() throws Exception {

        List<ItemResponse> items =
                List.of(
                        new ItemResponse(1L, 5),
                        new ItemResponse(2L, 10)
                );

        when(productService.getItemsByProductId(1L))
                .thenReturn(items);

        mockMvc.perform(
                get("/api/v1/products/1/items")
        )
        .andExpect(status().isOk());

        verify(productService)
                .getItemsByProductId(1L);
    }

    @Test
    void getProductsWithoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/products")
        )
        .andExpect(status().isUnauthorized());

        verify(productService, never())
                .getAllProducts(anyInt(), anyInt());
    }
}