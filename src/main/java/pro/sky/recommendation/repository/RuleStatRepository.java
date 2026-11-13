package pro.sky.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.sky.recommendation.model.RuleStat;

import java.util.UUID;

/**
 * Репозиторий статистики срабатываний динамических правил (PostgreSQL).
 */
public interface RuleStatRepository extends JpaRepository<RuleStat, UUID> {

    /**
     * Атомарно увеличивает счётчик правила на 1; если строки ещё нет, создаёт её со значением 1.
     *
     * <p>Upsert выполняется одним запросом внутри БД, поэтому одновременные срабатывания
     * не затирают друг друга (нет схемы «прочитал в Java, прибавил, записал»).</p>
     *
     * @param ruleId идентификатор сработавшего правила
     */
    @Modifying
    @Query(value = "INSERT INTO rule_stat (rule_id, trigger_count) VALUES (:ruleId, 1) "
            + "ON CONFLICT (rule_id) DO UPDATE SET trigger_count = rule_stat.trigger_count + 1",
            nativeQuery = true)
    void increment(@Param("ruleId") UUID ruleId);
}
