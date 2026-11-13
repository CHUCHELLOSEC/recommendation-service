package pro.sky.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Ответ {@code GET /recommendation/{user_id}}: {@code {"user_id": "...", "recommendations": [...]}}.
 */
public class RecommendationsResponse {

    @JsonProperty("user_id")
    private final UUID userId;

    private final List<RecommendationDto> recommendations;

    /**
     * Создаёт ответ.
     *
     * @param userId          идентификатор клиента
     * @param recommendations рекомендованные продукты
     */
    public RecommendationsResponse(UUID userId, List<RecommendationDto> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }

    /**
     * @return идентификатор клиента
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * @return рекомендованные продукты
     */
    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }
}
