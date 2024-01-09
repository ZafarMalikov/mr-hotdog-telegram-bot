package com.example.mr_hotdog_telegram_bot.user;

import com.example.mr_hotdog_telegram_bot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public void create(User user){
        Optional<User> optionalUser = repository.findAll().stream()
                .filter(user1 -> user1.getPhoneNumber().equals(user.getPhoneNumber()))
                .findFirst();

        if (optionalUser.isEmpty()) {
            repository.save(user);
        } else {
            System.out.println("Bunday user mavjud");
        }
    }

    public User findByChatId(Long chatId){
        Optional<User> first = repository.findAll().stream().filter(user -> user.getChatId().equals(chatId)).findFirst();
        return first.orElse(null);
    }




    public void update(User updateUser,long chatId){
        Optional<User> optionalUser = repository.findUserByChatId(chatId);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            if (updateUser.getOrderType()!=null){
                user.setOrderType(updateUser.getOrderType());
            }
            if (updateUser.getLocation()!=null){
                user.setLocation(updateUser.getLocation());
            }
            if (updateUser.getPayType()!=null){
                user.setPayType(updateUser.getPayType());
            }
         repository.save(user);
        }
    }




}
