package com.example.mr_hotdog_telegram_bot.order.entity;

import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@RedisHash(timeToLive = 3600)
public class Order {

    @Id
    private long userChatId;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<Product> products;

}
