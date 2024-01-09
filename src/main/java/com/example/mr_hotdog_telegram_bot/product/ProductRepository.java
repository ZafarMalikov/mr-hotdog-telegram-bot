package com.example.mr_hotdog_telegram_bot.product;

import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {

    Optional<Product>findByCallbackDataName(String data);
}
