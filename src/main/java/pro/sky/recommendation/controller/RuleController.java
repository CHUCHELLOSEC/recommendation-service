package pro.sky.recommendation.controller;

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

@RestController
@RequestMapping("/rule")
public class RuleController {

    private final DynamicRuleService service;
    private final RuleStatService statService; // НОВОЕ

    public RuleController(DynamicRuleService service, RuleStatService statService) { // ИЗМЕНЕНО
        this.service = service;
        this.statService = statService; // НОВОЕ
    }

    @PostMapping
    public DynamicRule add(@RequestBody DynamicRule rule) {
        return service.add(rule);
    }

    @GetMapping
    public Map<String, List<DynamicRule>> getAll() {
        return Map.of("data", service.findAll());
    }

    // НОВОЕ: GET /rule/stats
    @GetMapping("/stats")
    public Map<String, List<RuleStatResponse>> getStats() {
        return Map.of("stats", statService.getAll());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        service.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}