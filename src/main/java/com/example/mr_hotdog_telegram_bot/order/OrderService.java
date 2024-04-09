package com.example.mr_hotdog_telegram_bot.order;

import com.example.mr_hotdog_telegram_bot.order.entity.Order;
import com.example.mr_hotdog_telegram_bot.user.entity.PayType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    public Order findById(String productId) {
       return orderRepository.findById(Integer.valueOf(productId)).get();
    }

    public List<Order>findAllOrder(Integer userChatId){
        return orderRepository.findOrdersByUserChatId(userChatId);
    }

    @Transactional
    public void deleteUserOrders(Integer userChatId){
        orderRepository.deleteAllOrderByUserChatId(userChatId);
    }

    public void addOrder(Order order){
        orderRepository.save(order);
    }

    public void updatePayType(long order, PayType payType) {
      orderRepository.findOrdersByUserChatId((int) order).forEach(order1 -> {
          order1.setPayType(payType);
          orderRepository.save(order1);
      });

    }
}
