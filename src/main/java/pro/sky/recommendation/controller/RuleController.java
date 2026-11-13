package pro.sky.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.dto.RuleStatResponse;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.service.DynamicRuleService;
import pro.sky.recommendation.service.RuleStatService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API управления динамическими правилами и статистикой их срабатываний.
 */
@RestController
@RequestMapping("/rule")
@Tag(name = "Dynamic rules", description = "Управление динамическими правилами рекомендаций")
public class RuleController {

    private final DynamicRuleService service;
    private final RuleStatService statService;

    /**
     * Создаёт контроллер.
     *
     * @param service     сервис динамических правил
     * @param statService сервис статистики срабатываний
     */
    public RuleController(DynamicRuleService service, RuleStatService statService) {
        this.service = service;
        this.statService = statService;
    }

    /**
     * {@code POST /rule}: создаёт динамическое правило.
     *
     * @param rule правило без {@code id}
     * @return сохранённое правило с выданным сервером {@code id}
     */
    @PostMapping
    @Operation(summary = "Создать динамическое правило")
    public DynamicRule add(@RequestBody DynamicRule rule) {
        return service.add(rule);
    }

    /**
     * {@code GET /rule}: все динамические правила.
     *
     * @return {@code {"data": [...]}}
     */
    @GetMapping
    @Operation(summary = "Получить все динамические правила")
    public Map<String, List<DynamicRule>> getAll() {
        return Map.of("data", service.findAll());
    }

    /**
     * {@code GET /rule/stats}: число срабатываний каждого динамического правила.
     *
     * @return {@code {"stats": [{"rule_id": "...", "count": N}]}}; правила без срабатываний имеют {@code count = 0}
     */
    @GetMapping("/stats")
    @Operation(summary = "Получить статистику срабатываний правил")
    public Map<String, List<RuleStatResponse>> getStats() {
        return Map.of("stats", statService.getAll());
    }

    /**
     * {@code DELETE /rule/{product_id}}: удаляет правила продукта вместе с их статистикой.
     *
     * @param productId идентификатор продукта
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Удалить динамическое правило продукта")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        service.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
