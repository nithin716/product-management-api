package com.zest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zest.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByProductId(Long productId);
}