package com.zest.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.zest.entity.Product;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryH2Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void saveAndFindProduct_shouldWorkWithH2() {

        Product product = new Product();

        product.setProductName("H2 Test Laptop");
        product.setCreatedBy("test-user");
        product.setCreatedOn(LocalDateTime.now());

        Product savedProduct =
                productRepository.save(product);

        assertNotNull(savedProduct.getId());

        Product foundProduct =
                productRepository
                        .findById(savedProduct.getId())
                        .orElseThrow();

        assertEquals(
                "H2 Test Laptop",
                foundProduct.getProductName()
        );

        assertEquals(
                "test-user",
                foundProduct.getCreatedBy()
        );
    }
}