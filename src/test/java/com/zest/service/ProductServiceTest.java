package com.zest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.zest.dto.item.ItemResponse;
import com.zest.dto.product.CreateProductRequest;
import com.zest.dto.product.ProductResponse;
import com.zest.dto.product.UpdateProductRequest;

import com.zest.entity.Item;
import com.zest.entity.Product;

import com.zest.exception.ProductNotFoundException;

import com.zest.repository.ItemRepository;
import com.zest.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {

        product = new Product();

        product.setId(1L);
        product.setProductName("Laptop");
        product.setCreatedBy("admin");
        product.setCreatedOn(LocalDateTime.now());
    }

    @Test
    void createProduct_shouldCreateProductSuccessfully() {

        CreateProductRequest request =
                new CreateProductRequest("Laptop");

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse response =
                productService.createProduct(
                        request,
                        "admin"
                );

        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getProductName());
        assertEquals("admin", response.getCreatedBy());

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void getProductById_shouldReturnProduct() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getProductName());

        verify(productRepository, times(1))
                .findById(1L);
    }

    @Test
    void getProductById_shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        verify(productRepository, times(1))
                .findById(999L);
    }

    @Test
    void getAllProducts_shouldReturnPaginatedProducts() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(productPage);

        Page<ProductResponse> response =
                productService.getAllProducts(0, 10);

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                "Laptop",
                response.getContent()
                        .get(0)
                        .getProductName()
        );

        verify(productRepository, times(1))
                .findAll(any(Pageable.class));
    }

    @Test
    void updateProduct_shouldUpdateProductSuccessfully() {

        UpdateProductRequest request =
                new UpdateProductRequest("Gaming Laptop");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProduct(
                        1L,
                        request,
                        "admin"
                );

        assertEquals(
                "Gaming Laptop",
                response.getProductName()
        );

        assertEquals(
                "admin",
                product.getModifiedBy()
        );

        assertEquals(
                "Gaming Laptop",
                product.getProductName()
        );

        verify(productRepository, times(1))
                .findById(1L);

        verify(productRepository, times(1))
                .save(product);
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound() {

        UpdateProductRequest request =
                new UpdateProductRequest("Gaming Laptop");

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(
                        999L,
                        request,
                        "admin"
                )
        );

        verify(productRepository, times(1))
                .findById(999L);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProductSuccessfully() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository, times(1))
                .findById(1L);

        verify(productRepository, times(1))
                .delete(product);
    }

    @Test
    void deleteProduct_shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        verify(productRepository, times(1))
                .findById(999L);

        verify(productRepository, never())
                .delete(any(Product.class));
    }

    @Test
    void getItemsByProductId_shouldReturnItems() {

        Item item1 = new Item();

        item1.setId(1L);
        item1.setProduct(product);
        item1.setQuantity(5);

        Item item2 = new Item();

        item2.setId(2L);
        item2.setProduct(product);
        item2.setQuantity(10);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(itemRepository.findByProductId(1L))
                .thenReturn(List.of(item1, item2));

        List<ItemResponse> response =
                productService.getItemsByProductId(1L);

        assertEquals(
                2,
                response.size()
        );

        assertEquals(
                5,
                response.get(0).getQuantity()
        );

        assertEquals(
                10,
                response.get(1).getQuantity()
        );

        verify(productRepository, times(1))
                .findById(1L);

        verify(itemRepository, times(1))
                .findByProductId(1L);
    }

    @Test
    void getItemsByProductId_shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getItemsByProductId(999L)
        );

        verify(productRepository, times(1))
                .findById(999L);

        verify(itemRepository, never())
                .findByProductId(anyLong());
    }
}