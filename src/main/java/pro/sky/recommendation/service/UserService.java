package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.BankUser;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.List;
import java.util.Optional;

/**
 * Поиск клиентов банка для Telegram-бота.
 */
@Service
public class UserService {

    private final RecommendationsRepository repository;

    /**
     * Создаёт сервис.
     *
     * @param repository репозиторий основной БД
     */
    public UserService(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * Находит клиента по имени пользователя, только если совпадение ровно одно.
     *
     * @param username имя пользователя
     * @return клиент; пустой результат, если совпадений ноль или несколько
     */
    public Optional<BankUser> findUniqueByUsername(String username) {
        List<BankUser> users = repository.findUsersByUsername(username);
        return users.size() == 1 ? Optional.of(users.get(0)) : Optional.empty();
    }
}
