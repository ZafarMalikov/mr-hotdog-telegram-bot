package com.example.mr_hotdog_telegram_bot.user;


import com.example.mr_hotdog_telegram_bot.product.entity.Product;
import com.example.mr_hotdog_telegram_bot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>  {
    Optional<User>findUserByChatId(long chatId);

    List<User> findAllById(long chatId);
}
