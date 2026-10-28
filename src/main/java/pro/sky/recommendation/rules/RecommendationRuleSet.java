package pro.sky.recommendation.rules;

import pro.sky.recommendation.dto.RecommendationDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Набор правил одного продукта.
 * Если пользователь подходит, возвращает рекомендацию, иначе пустой Optional.
 */
public interface RecommendationRuleSet {

    Optional<RecommendationDto> check(UUID userId);
}
//test//test