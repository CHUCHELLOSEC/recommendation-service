package pro.sky.recommendation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.recommendation.dto.RuleStatResponse;
import pro.sky.recommendation.repository.DynamicRuleRepository;
import pro.sky.recommendation.repository.RuleStatRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Статистика срабатываний динамических правил.
 */
@Service
public class RuleStatService {

    private final RuleStatRepository statRepository;
    private final DynamicRuleRepository ruleRepository;

    /**
     * Создаёт сервис.
     *
     * @param statRepository репозиторий статистики
     * @param ruleRepository репозиторий динамических правил
     */
    public RuleStatService(RuleStatRepository statRepository, DynamicRuleRepository ruleRepository) {
        this.statRepository = statRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Увеличивает счётчик срабатываний правила на 1 одним атомарным запросом.
     *
     * @param ruleId идентификатор сработавшего правила
     */
    @Transactional("rulesTransactionManager")
    public void increment(UUID ruleId) {
        statRepository.increment(ruleId);
    }

    /**
     * Статистика по всем правилам. Список строится от таблицы правил,
     * поэтому правило без срабатываний попадает в ответ со значением 0.
     *
     * @return число срабатываний для каждого существующего правила
     */
    @Transactional(value = "rulesTransactionManager", readOnly = true)
    public List<RuleStatResponse> getAll() {
        Map<UUID, Long> counts = new HashMap<>();
        statRepository.findAll()
                .forEach(stat -> counts.put(stat.getRule().getId(), stat.getCount()));

        return ruleRepository.findAll().stream()
                .map(rule -> new RuleStatResponse(rule.getId(), counts.getOrDefault(rule.getId(), 0L)))
                .toList();
    }
}
