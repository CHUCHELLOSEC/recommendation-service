package pro.sky.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.service.RecommendationService;

import java.util.UUID;

/**
 * REST API получения рекомендаций для клиента.
 */
@RestController
@Tag(name = "Recommendations", description = "Рекомендации банковских продуктов")
public class RecommendationController {

    private final RecommendationService service;

    /**
     * Создаёт контроллер.
     *
     * @param service сервис подбора рекомендаций
     */
    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    /**
     * {@code GET /recommendation/{user_id}}: рекомендации для клиента по статическим и динамическим правилам.
     *
     * @param userId идентификатор клиента
     * @return идентификатор клиента и список рекомендованных продуктов (может быть пустым)
     */
    @GetMapping("/recommendation/{user_id}")
    @Operation(summary = "Получить рекомендации для клиента")
    public RecommendationsResponse getRecommendations(
            @Parameter(description = "Идентификатор клиента") @PathVariable("user_id") UUID userId) {
        return service.getRecommendations(userId);
    }
}
