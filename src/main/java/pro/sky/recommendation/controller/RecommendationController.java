package pro.sky.recommendation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.service.RecommendationService;
import java.util.UUID;

@RestController
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/recommendation/{user_id}")
    public RecommendationsResponse getRecommendations(@PathVariable("user_id") UUID userId) {
        return service.getRecommendations(userId);
    }
}
