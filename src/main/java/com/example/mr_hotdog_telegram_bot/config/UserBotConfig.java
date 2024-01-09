package com.example.mr_hotdog_telegram_bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class UserBotConfig {

    @Value("${telegram.bot.user.token}")
    String token;

    @Value("${telegram.bot.user.name}")
    String botName;
}
