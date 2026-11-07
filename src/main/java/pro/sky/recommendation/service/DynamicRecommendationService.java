package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DynamicRecommendationService {

    private final DynamicRuleRepository repository;
    private final RuleQueryEvaluator evaluator;

    public DynamicRecommendationService(DynamicRuleRepository repository, RuleQueryEvaluator evaluator) {
        this.repository = repository;
        this.evaluator = evaluator;
    }

    /** Все продукты, чьи динамические правила выполняются для этого пользователя. */
    public List<RecommendationDto> check(UUID userId) {
        return repository.findAll().stream()
                .filter(rule -> isSatisfied(userId, rule))
                .map(rule -> new RecommendationDto(
                        rule.getProductName(), rule.getProductId(), rule.getProductText()))
                .toList();
    }

    // Правило выполняется, когда ВСЕ его запросы вернули true (Q1 && Q2 && Q3)
    private boolean isSatisfied(UUID userId, DynamicRule rule) {
        return rule.getRule().stream()
                .allMatch(query -> evaluator.evaluate(userId, query));
    }
}