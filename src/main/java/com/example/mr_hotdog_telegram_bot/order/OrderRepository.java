package com.example.mr_hotdog_telegram_bot.order;

import com.example.mr_hotdog_telegram_bot.order.entity.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends CrudRepository<Order,Long> {

    Optional<Order> findOrderByUserChatId(Long aLong);
}
