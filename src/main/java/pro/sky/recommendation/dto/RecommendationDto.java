package pro.sky.recommendation.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Одна рекомендация: продукт банка, который предлагается клиенту.
 * Объект неизменяемый.
 */
public class RecommendationDto {

    private final String name;
    private final UUID id;
    private final String text;

    /**
     * Создаёт рекомендацию.
     *
     * @param name название продукта
     * @param id   идентификатор продукта
     * @param text текст рекомендации
     */
    public RecommendationDto(String name, UUID id, String text) {
        this.name = name;
        this.id = id;
        this.text = text;
    }

    /**
     * @return название продукта
     */
    public String getName() {
        return name;
    }

    /**
     * @return идентификатор продукта
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return текст рекомендации
     */
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecommendationDto that = (RecommendationDto) o;
        return Objects.equals(name, that.name)
                && Objects.equals(id, that.id)
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, text);
    }
}
