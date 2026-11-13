package pro.sky.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Одна строка статистики в ответе {@code GET /rule/stats}: {@code {"rule_id": "...", "count": 5}}.
 *
 * @param ruleId идентификатор динамического правила
 * @param count  сколько раз правило сработало
 */
public record RuleStatResponse(@JsonProperty("rule_id") UUID ruleId, long count) {
}
