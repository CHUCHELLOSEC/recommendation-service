package pro.sky.recommendation.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.BankUser;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.service.RecommendationService;
import pro.sky.recommendation.service.UserService;

import java.util.List;
import java.util.Optional;

@Component
public class RecommendationBot implements UpdatesListener {

    private static final Logger log = LoggerFactory.getLogger(RecommendationBot.class);

    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private static final String WELCOME_AND_HELP = """
            Здравствуйте! Я бот-рекомендатель банка «Стар».
            Я подбираю банковские продукты, которые вам подойдут.

            Справка. Доступная команда:
            /recommend username — показать рекомендации для клиента с таким именем пользователя.

            Пример: /recommend ivanov""";

    private final TelegramBot telegramBot;
    private final UserService userService;
    private final RecommendationService recommendationService;
    private final String token;

    public RecommendationBot(TelegramBot telegramBot,
                             UserService userService,
                             RecommendationService recommendationService,
                             @Value("${telegram.bot.token:}") String token) {
        this.telegramBot = telegramBot;
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.token = token;
    }

    // Выполняется один раз после создания бина: подписываем класс на сообщения
    @PostConstruct
    public void init() {
        if (token.isBlank()) {
            log.warn("Токен Telegram-бота не задан (переменная TELEGRAM_BOT_TOKEN), бот отключён");
            return;
        }
        telegramBot.setUpdatesListener(this);
    }

    // Telegram присылает сообщения пачкой, обрабатываем каждое
    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                handle(update);
            } catch (Exception e) {
                // Ошибка в одном сообщении не должна останавливать бота
                log.error("Ошибка при обработке сообщения", e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handle(Update update) {
        Message message = update.message();
        if (message == null || message.text() == null) {
            return;
        }

        long chatId = message.chat().id();
        String[] parts = message.text().trim().split("\\s+", 2);
        String command = parts[0];

        if (command.equals("/recommend") || command.startsWith("/recommend@")) {
            if (parts.length < 2 || parts[1].isBlank()) {
                send(chatId, "Укажите имя пользователя: /recommend username");
                return;
            }
            send(chatId, buildRecommendations(parts[1].trim()));
        } else {
            // /start и любое другое сообщение: приветствие и справка
            send(chatId, WELCOME_AND_HELP);
        }
    }

    private String buildRecommendations(String username) {
        // Ровно один пользователь: идём дальше. Ноль или несколько: «Пользователь не найден»
        Optional<BankUser> user = userService.findUniqueByUsername(username);
        if (user.isEmpty()) {
            return USER_NOT_FOUND;
        }

        BankUser found = user.get();
        List<RecommendationDto> recommendations =
                recommendationService.getRecommendations(found.id()).getRecommendations();

        StringBuilder text = new StringBuilder()
                .append("Здравствуйте ").append(found.firstName()).append(' ').append(found.lastName())
                .append("\n\nНовые продукты для вас:\n");

        if (recommendations.isEmpty()) {
            text.append("Пока подходящих продуктов нет.");
        } else {
            for (RecommendationDto recommendation : recommendations) {
                text.append("• ").append(recommendation.getName()).append('\n');
            }
        }
        return text.toString().trim();
    }

    private void send(long chatId, String text) {
        SendResponse response = telegramBot.execute(new SendMessage(chatId, text));
        if (!response.isOk()) {
            log.error("Не удалось отправить сообщение: код {}", response.errorCode());
        }
    }
}