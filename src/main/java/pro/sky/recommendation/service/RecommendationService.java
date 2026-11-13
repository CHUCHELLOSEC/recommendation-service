package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.rules.RecommendationRuleSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Главный сервис подбора рекомендаций: объединяет статические и динамические правила.
 */
@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRecommendationService dynamicService;

    /**
     * Создаёт сервис.
     *
     * @param ruleSets       все статические правила (бины {@link RecommendationRuleSet})
     * @param dynamicService сервис динамических правил
     */
    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 DynamicRecommendationService dynamicService) {
        this.ruleSets = ruleSets;
        this.dynamicService = dynamicService;
    }

    /**
     * Подбирает рекомендации: сначала статические правила, затем динамические.
     * Каждый продукт попадает в ответ не больше одного раза.
     *
     * @param userId идентификатор клиента
     * @return ответ с рекомендациями (список может быть пустым)
     */
    public RecommendationsResponse getRecommendations(UUID userId) {
        // LinkedHashMap по id продукта сохраняет порядок и убирает дубликаты,
        // если продукт есть и в статических, и в динамических правилах
        Map<UUID, RecommendationDto> result = new LinkedHashMap<>();

        ruleSets.stream()
                .map(ruleSet -> ruleSet.check(userId))
                .flatMap(Optional::stream)
                .forEach(dto -> result.putIfAbsent(dto.getId(), dto));

        dynamicService.check(userId)
                .forEach(dto -> result.putIfAbsent(dto.getId(), dto));

        return new RecommendationsResponse(userId, new ArrayList<>(result.values()));
    }
}
