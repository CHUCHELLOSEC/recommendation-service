package pro.sky.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Ответ REST API: {"user_id": "...", "recommendations": [...]}
 */
public class RecommendationsResponse {

    @JsonProperty("user_id")
    private final UUID userId;

    private final List<RecommendationDto> recommendations;

    public RecommendationsResponse(UUID userId, List<RecommendationDto> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }
}
//test//test