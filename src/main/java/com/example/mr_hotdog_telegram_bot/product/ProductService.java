package com.example.mr_hotdog_telegram_bot.product;

import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import com.example.mr_hotdog_telegram_bot.product.entity.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllByProductType(ProductType productType) {
       return productRepository.findAll().stream()
               .filter(product -> product.getProductType().equals(productType)).toList();
    }


    public Optional<Product> findById(String productId) {
        return productRepository.findById(Integer.valueOf(productId));
    }

    public void updateProductCount(int count,Integer productId){
        Optional<Product> byId = productRepository.findById(productId);
        if (byId.isPresent()){
            Product product = byId.get();
            product.setCount(count);
            productRepository.save(product);
        }
    }





}
