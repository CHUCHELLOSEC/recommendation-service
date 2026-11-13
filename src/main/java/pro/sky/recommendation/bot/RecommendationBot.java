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

/**
 * Telegram-бот, который выдаёт клиенту рекомендации по команде {@code /recommend username}.
 *
 * <p>Любое другое сообщение (в том числе {@code /start}) получает приветствие и справку.
 * Если токен бота не задан, бот не подписывается на обновления, а сервис продолжает работать.</p>
 */
@Component
public class RecommendationBot implements UpdatesListener {

    private static final Logger log = LoggerFactory.getLogger(RecommendationBot.class);

    private static final String RECOMMEND_COMMAND = "/recommend";

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

    /**
     * Создаёт бота.
     *
     * @param telegramBot           клиент Telegram Bot API
     * @param userService           поиск клиента банка по имени пользователя
     * @param recommendationService подбор рекомендаций
     * @param token                 токен бота; пустая строка отключает бота
     */
    public RecommendationBot(TelegramBot telegramBot,
                             UserService userService,
                             RecommendationService recommendationService,
                             @Value("${telegram.bot.token:}") String token) {
        this.telegramBot = telegramBot;
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.token = token;
    }

    /**
     * Подписывает бота на обновления Telegram после создания бина.
     * Без токена подписка не выполняется.
     */
    @PostConstruct
    public void init() {
        if (token.isBlank()) {
            log.warn("Токен Telegram-бота не задан (переменная TELEGRAM_BOT_TOKEN), бот отключён");
            return;
        }
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Обрабатывает пачку обновлений от Telegram.
     * Ошибка в одном сообщении логируется и не останавливает обработку остальных.
     *
     * @param updates обновления, полученные от Telegram
     * @return {@link UpdatesListener#CONFIRMED_UPDATES_ALL}: все обновления подтверждены
     */
    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                handle(update);
            } catch (Exception e) {
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

        // В групповых чатах команда приходит в виде /recommend@BotName
        if (command.equals(RECOMMEND_COMMAND) || command.startsWith(RECOMMEND_COMMAND + "@")) {
            if (parts.length < 2 || parts[1].isBlank()) {
                send(chatId, "Укажите имя пользователя: /recommend username");
                return;
            }
            send(chatId, buildRecommendations(parts[1].trim()));
        } else {
            send(chatId, WELCOME_AND_HELP);
        }
    }

    private String buildRecommendations(String username) {
        // Ровно одно совпадение: показываем рекомендации. Ноль или несколько: «Пользователь не найден»
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
