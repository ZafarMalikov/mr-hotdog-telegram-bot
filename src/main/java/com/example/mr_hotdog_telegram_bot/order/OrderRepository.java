package com.example.mr_hotdog_telegram_bot.order;

import com.example.mr_hotdog_telegram_bot.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findOrdersByUserChatId(Integer userChatId);


    void deleteAllOrderByUserChatId(Integer userChatId);
}
