package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.rules.RecommendationRuleSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;

    // Spring сам соберёт сюда ВСЕ бины, реализующие RecommendationRuleSet
    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public RecommendationsResponse getRecommendations(UUID userId) {
        List<RecommendationDto> recommendations = ruleSets.stream()
                .map(ruleSet -> ruleSet.check(userId))   // у каждого набора правил спрашиваем
                .flatMap(Optional::stream)               // оставляем только непустые результаты
                .toList();

        return new RecommendationsResponse(userId, recommendations);
    }
}
