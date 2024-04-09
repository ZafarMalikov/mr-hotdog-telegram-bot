package com.example.mr_hotdog_telegram_bot.user.entity;


import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String phoneNumber;

    private String name;
    private Integer count;

    @Column(unique = true)
    private Long chatId;
    private Location location;
    private String orderType;
    @Enumerated(EnumType.STRING)
    private PayType payType;

}
