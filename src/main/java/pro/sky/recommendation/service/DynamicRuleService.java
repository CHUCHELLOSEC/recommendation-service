package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.model.RuleQuery;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {

    private final DynamicRuleRepository repository;
    private final RuleQueryEvaluator evaluator;

    public DynamicRuleService(DynamicRuleRepository repository, RuleQueryEvaluator evaluator) {
        this.repository = repository;
        this.evaluator = evaluator;
    }

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
        rule.setId(null); // идентификатор выдаёт сервер, а не клиент
        return repository.save(rule);
    }

    @Transactional(value = "rulesTransactionManager", readOnly = true)
    public List<DynamicRule> findAll() {
        return repository.findAll();
    }

    @Transactional("rulesTransactionManager")
    public void deleteByProductId(UUID productId) {
        repository.deleteByProductId(productId);
    }
}