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

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRecommendationService dynamicService;

    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 DynamicRecommendationService dynamicService) {
        this.ruleSets = ruleSets;
        this.dynamicService = dynamicService;
    }

    public RecommendationsResponse getRecommendations(UUID userId) {
        // LinkedHashMap по id продукта: один продукт не попадёт в ответ дважды,
        // даже если он есть и в фиксированных, и в динамических правилах
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