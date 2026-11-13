package pro.sky.recommendation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Счётчик срабатываний одного динамического правила (таблица {@code rule_stat}).
 * При удалении правила строка удаляется каскадно на уровне БД.
 */
@Entity
@Table(name = "rule_stat")
public class RuleStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule rule;

    @Column(name = "trigger_count", nullable = false)
    private long count;

    /**
     * Конструктор для JPA.
     */
    public RuleStat() {
    }

    /**
     * @return идентификатор строки статистики
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id идентификатор строки статистики
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return правило, к которому относится счётчик
     */
    public DynamicRule getRule() {
        return rule;
    }

    /**
     * @param rule правило, к которому относится счётчик
     */
    public void setRule(DynamicRule rule) {
        this.rule = rule;
    }

    /**
     * @return число срабатываний
     */
    public long getCount() {
        return count;
    }

    /**
     * @param count число срабатываний
     */
    public void setCount(long count) {
        this.count = count;
    }
}
