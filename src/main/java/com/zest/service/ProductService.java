package com.zest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zest.dto.item.ItemResponse;
import com.zest.dto.product.CreateProductRequest;
import com.zest.dto.product.ProductResponse;
import com.zest.dto.product.UpdateProductRequest;
import com.zest.entity.Item;
import com.zest.entity.Product;
import com.zest.exception.ProductNotFoundException;
import com.zest.repository.ItemRepository;
import com.zest.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    public ProductService(
            ProductRepository productRepository,
            ItemRepository itemRepository) {

        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
    }

    public ProductResponse createProduct(
            CreateProductRequest request,
            String username) {

        Product product = new Product();

        product.setProductName(request.getProductName());
        product.setCreatedBy(username);
        product.setCreatedOn(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage =
                productRepository.findAll(pageable);

        return productPage.map(this::mapToResponse);
    }

    public ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request,
            String username) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        product.setProductName(request.getProductName());
        product.setModifiedBy(username);
        product.setModifiedOn(LocalDateTime.now());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Long productId) {

        productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId));

        List<Item> items =
                itemRepository.findByProductId(productId);

        return items.stream()
                .map(this::mapToItemResponse)
                .toList();
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn()
        );
    }

    private ItemResponse mapToItemResponse(Item item) {

        return new ItemResponse(
                item.getId(),
                item.getQuantity()
        );
    }
}