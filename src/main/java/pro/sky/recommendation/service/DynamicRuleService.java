package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.model.RuleQuery;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import java.util.List;
import java.util.UUID;

/**
 * Управление динамическими правилами: создание с валидацией, чтение, удаление.
 */
@Service
public class DynamicRuleService {

    private final DynamicRuleRepository repository;
    private final RuleQueryEvaluator evaluator;

    /**
     * Создаёт сервис.
     *
     * @param repository репозиторий динамических правил
     * @param evaluator  валидатор запросов правила
     */
    public DynamicRuleService(DynamicRuleRepository repository, RuleQueryEvaluator evaluator) {
        this.repository = repository;
        this.evaluator = evaluator;
    }

    /**
     * Проверяет и сохраняет новое правило. Идентификатор всегда выдаёт сервер.
     *
     * @param rule правило из запроса
     * @return сохранённое правило с {@code id}
     * @throws IllegalArgumentException если обязательные поля не заданы или запрос правила некорректен
     */
    @Transactional("rulesTransactionManager")
    public DynamicRule add(DynamicRule rule) {
        if (rule.getProductName() == null || rule.getProductId() == null || rule.getProductText() == null) {
            throw new IllegalArgumentException("product_name, product_id и product_text обязательны");
        }
        if (rule.getRule() == null || rule.getRule().isEmpty()) {
            throw new IllegalArgumentException("Правило должно содержать хотя бы один запрос");
        }
        for (RuleQuery query : rule.getRule()) {
            evaluator.validate(query);
        }
        rule.setId(null);
        return repository.save(rule);
    }

    /**
     * @return все динамические правила
     */
    @Transactional(value = "rulesTransactionManager", readOnly = true)
    public List<DynamicRule> findAll() {
        return repository.findAll();
    }

    /**
     * Удаляет правила продукта. Статистика удаляется каскадно (FK {@code ON DELETE CASCADE}).
     *
     * @param productId идентификатор продукта
     */
    @Transactional("rulesTransactionManager")
    public void deleteByProductId(UUID productId) {
        repository.deleteByProductId(productId);
    }
}
