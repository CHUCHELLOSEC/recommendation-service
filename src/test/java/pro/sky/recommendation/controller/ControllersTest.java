package pro.sky.recommendation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.dto.RuleStatResponse;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.repository.RecommendationsRepository;
import pro.sky.recommendation.service.DynamicRuleService;
import pro.sky.recommendation.service.RecommendationService;
import pro.sky.recommendation.service.RuleStatService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({RecommendationController.class, RuleController.class, ManagementController.class})
class ControllersTest {

    private static final UUID USER = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
    private static final UUID PRODUCT = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;
    @MockBean
    private DynamicRuleService dynamicRuleService;
    @MockBean
    private RuleStatService ruleStatService;
    @MockBean
    private RecommendationsRepository recommendationsRepository;

    @Test
    void getRecommendations() throws Exception {
        when(recommendationService.getRecommendations(USER)).thenReturn(new RecommendationsResponse(
                USER, List.of(new RecommendationDto("Invest 500", PRODUCT, "text"))));

        mockMvc.perform(get("/recommendation/{id}", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(USER.toString()))
                .andExpect(jsonPath("$.recommendations[0].name").value("Invest 500"))
                .andExpect(jsonPath("$.recommendations[0].id").value(PRODUCT.toString()));
    }

    @Test
    void getRecommendationsRejectsInvalidUuid() throws Exception {
        mockMvc.perform(get("/recommendation/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRule() throws Exception {
        DynamicRule saved = new DynamicRule();
        saved.setId(UUID.randomUUID());
        saved.setProductId(PRODUCT);
        saved.setProductName("Простой кредит");
        saved.setProductText("text");
        when(dynamicRuleService.add(any())).thenReturn(saved);

        String body = """
                {
                  "product_name": "Простой кредит",
                  "product_id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
                  "product_text": "text",
                  "rule": [{"query": "USER_OF", "arguments": ["CREDIT"], "negate": true}]
                }""";

        mockMvc.perform(post("/rule").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.product_id").value(PRODUCT.toString()));
    }

    @Test
    void createRuleReturns400OnValidationError() throws Exception {
        when(dynamicRuleService.add(any())).thenThrow(new IllegalArgumentException("Неизвестный тип запроса"));

        mockMvc.perform(post("/rule").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Неизвестный тип запроса"));
    }

    @Test
    void getAllRules() throws Exception {
        when(dynamicRuleService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void deleteRule() throws Exception {
        mockMvc.perform(delete("/rule/{productId}", PRODUCT))
                .andExpect(status().isNoContent());

        verify(dynamicRuleService).deleteByProductId(PRODUCT);
    }

    @Test
    void getStats() throws Exception {
        UUID ruleId = UUID.randomUUID();
        when(ruleStatService.getAll()).thenReturn(List.of(new RuleStatResponse(ruleId, 3)));

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats[0].rule_id").value(ruleId.toString()))
                .andExpect(jsonPath("$.stats[0].count").value(3));
    }

    @Test
    void clearCaches() throws Exception {
        mockMvc.perform(post("/management/clear-caches"))
                .andExpect(status().isOk());

        verify(recommendationsRepository).clearCaches();
    }

    @Test
    void info() throws Exception {
        mockMvc.perform(get("/management/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.version").isNotEmpty());
    }
}
