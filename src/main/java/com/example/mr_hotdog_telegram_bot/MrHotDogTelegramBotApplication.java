package com.example.mr_hotdog_telegram_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;



@SpringBootApplication
public class MrHotDogTelegramBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MrHotDogTelegramBotApplication.class, args);
	}

}
