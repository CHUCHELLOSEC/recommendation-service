package pro.sky.recommendation.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Создаёт клиент Telegram Bot API.
 */
@Configuration
public class TelegramBotConfiguration {

    @Value("${telegram.bot.token:}")
    private String token;

    /**
     * Клиент Telegram Bot API. При создании бина запросов в Telegram не выполняется,
     * поэтому сервис запускается даже без сети или без токена.
     *
     * @return клиент Telegram Bot API
     */
    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }
}
