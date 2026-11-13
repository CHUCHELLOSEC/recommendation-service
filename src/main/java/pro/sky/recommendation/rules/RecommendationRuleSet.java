package pro.sky.recommendation.rules;

import pro.sky.recommendation.dto.RecommendationDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Статическое правило (набор условий) одного продукта, зашитое в код.
 *
 * <p>Чтобы добавить новое статическое правило, достаточно создать Spring-бин ({@code @Component}),
 * реализующий этот интерфейс: {@code RecommendationService} получает все реализации автоматически.</p>
 */
public interface RecommendationRuleSet {

    /**
     * Проверяет, подходит ли продукт клиенту.
     *
     * @param userId идентификатор клиента
     * @return рекомендация, если все условия выполнены, иначе пустой {@link Optional}
     */
    Optional<RecommendationDto> check(UUID userId);
}
