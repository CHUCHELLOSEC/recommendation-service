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
 * Счётчик срабатываний одного динамического правила.
 */
@Entity
@Table(name = "rule_stat")
public class RuleStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Связь с правилом: колонка rule_id указывает на dynamic_rule.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule rule;

    @Column(name = "trigger_count", nullable = false)
    private long count;

    public RuleStat() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DynamicRule getRule() { return rule; }
    public void setRule(DynamicRule rule) { this.rule = rule; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}