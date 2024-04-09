package com.example.mr_hotdog_telegram_bot.order.entity;

import com.example.mr_hotdog_telegram_bot.product.entity.ProductType;
import com.example.mr_hotdog_telegram_bot.user.entity.PayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "`order`")
@Builder
//@RedisHash(timeToLive = 3600)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userChatId;
    private String name;
    private double prise;
    private String info;
    private Integer count;
    @Enumerated(EnumType.STRING)
    private ProductType productType;
    @Enumerated(EnumType.STRING)
    private PayType payType;


}
