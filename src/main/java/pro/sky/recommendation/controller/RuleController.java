package pro.sky.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.service.DynamicRuleService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class RuleController {

    private final DynamicRuleService service;

    public RuleController(DynamicRuleService service) {
        this.service = service;
    }

    @PostMapping
    public DynamicRule add(@RequestBody DynamicRule rule) {
        return service.add(rule);
    }

    @GetMapping
    public Map<String, List<DynamicRule>> getAll() {
        return Map.of("data", service.findAll());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        service.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}