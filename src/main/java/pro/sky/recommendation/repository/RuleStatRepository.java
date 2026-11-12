package pro.sky.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.sky.recommendation.model.RuleStat;

import java.util.UUID;

public interface RuleStatRepository extends JpaRepository<RuleStat, UUID> {

    /**
     * Атомарно увеличивает счётчик на 1. Если строки ещё нет, создаёт её со значением 1.
     * Всё выполняется одним запросом внутри базы, поэтому два одновременных срабатывания
     * не затрут друг друга (нет схемы «прочитал в Java, прибавил, записал»).
     */
    @Modifying
    @Query(value = "INSERT INTO rule_stat (rule_id, trigger_count) VALUES (:ruleId, 1) "
            + "ON CONFLICT (rule_id) DO UPDATE SET trigger_count = rule_stat.trigger_count + 1",
            nativeQuery = true)
    void increment(@Param("ruleId") UUID ruleId);
}