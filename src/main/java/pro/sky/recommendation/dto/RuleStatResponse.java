package pro.sky.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Одна строка статистики в ответе API: {"rule_id": "...", "count": 5}
 */
public record RuleStatResponse(@JsonProperty("rule_id") UUID ruleId, long count) {
}