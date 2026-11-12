package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicRecommendationService {

    private final DynamicRuleRepository repository;
    private final RuleQueryEvaluator evaluator;
    private final RuleStatService statService; // НОВОЕ

    public DynamicRecommendationService(DynamicRuleRepository repository,
                                        RuleQueryEvaluator evaluator,
                                        RuleStatService statService) { // ИЗМЕНЕНО: добавлен параметр
        this.repository = repository;
        this.evaluator = evaluator;
        this.statService = statService; // НОВОЕ
    }

    /** Все продукты, чьи динамические правила выполняются для этого пользователя. */
    public List<RecommendationDto> check(UUID userId) {
        List<RecommendationDto> result = new ArrayList<>(); // ИЗМЕНЕНО: был stream, стал цикл

        for (DynamicRule rule : repository.findAll()) {
            if (isSatisfied(userId, rule)) {
                statService.increment(rule.getId()); // НОВОЕ: именно это правило сработало, счётчик +1
                result.add(new RecommendationDto(
                        rule.getProductName(), rule.getProductId(), rule.getProductText()));
            }
        }
        return result;
    }

    // Правило выполняется, когда ВСЕ его запросы вернули true
    private boolean isSatisfied(UUID userId, DynamicRule rule) {
        return rule.getRule().stream()
                .allMatch(query -> evaluator.evaluate(userId, query));
    }
}