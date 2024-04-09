package com.example.mr_hotdog_telegram_bot.product.entity;

import com.example.mr_hotdog_telegram_bot.user.entity.PayType;
import com.example.mr_hotdog_telegram_bot.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private double prise;
    private String info;
    @Enumerated(EnumType.STRING)
    private ProductType productType;
    private Boolean isHave;


    private int count=1;
//    @Enumerated(EnumType.STRING)
//    private PayType payType;
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;


}
