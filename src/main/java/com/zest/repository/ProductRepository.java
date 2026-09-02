package com.zest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zest.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}