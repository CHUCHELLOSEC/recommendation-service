package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.BankUser;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final RecommendationsRepository repository;

    public UserService(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * Пользователь по имени пользователя, но только если он ровно один.
     * Ноль или несколько совпадений дают пустой результат.
     */
    public Optional<BankUser> findUniqueByUsername(String username) {
        List<BankUser> users = repository.findUsersByUsername(username);
        return users.size() == 1 ? Optional.of(users.get(0)) : Optional.empty();
    }
}