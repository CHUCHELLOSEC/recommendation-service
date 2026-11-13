package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Применяет динамические правила из PostgreSQL к клиенту и учитывает их срабатывания.
 */
@Service
public class DynamicRecommendationService {

    private final DynamicRuleRepository repository;
    private final RuleQueryEvaluator evaluator;
    private final RuleStatService statService;

    /**
     * Создаёт сервис.
     *
     * @param repository  репозиторий динамических правил
     * @param evaluator   вычислитель отдельных запросов правила
     * @param statService сервис статистики срабатываний
     */
    public DynamicRecommendationService(DynamicRuleRepository repository,
                                        RuleQueryEvaluator evaluator,
                                        RuleStatService statService) {
        this.repository = repository;
        this.evaluator = evaluator;
        this.statService = statService;
    }

    /**
     * Возвращает продукты, чьи динамические правила выполнены для клиента.
     * Для каждого сработавшего правила счётчик в статистике увеличивается на 1.
     *
     * @param userId идентификатор клиента
     * @return рекомендации по сработавшим правилам
     */
    public List<RecommendationDto> check(UUID userId) {
        List<RecommendationDto> result = new ArrayList<>();

        for (DynamicRule rule : repository.findAll()) {
            if (isSatisfied(userId, rule)) {
                statService.increment(rule.getId());
                result.add(new RecommendationDto(
                        rule.getProductName(), rule.getProductId(), rule.getProductText()));
            }
        }
        return result;
    }

    // Правило выполняется, когда ВСЕ его запросы вернули true; allMatch прерывается на первом false
    private boolean isSatisfied(UUID userId, DynamicRule rule) {
        return rule.getRule().stream()
                .allMatch(query -> evaluator.evaluate(userId, query));
    }
}
