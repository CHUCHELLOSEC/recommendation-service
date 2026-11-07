package pro.sky.recommendation.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Одна рекомендация: продукт банка, который предлагаем пользователю.
 * Объект неизменяемый: поля final, сеттеров нет.
 */
public class RecommendationDto {

    private final String name;
    private final UUID id;
    private final String text;

    public RecommendationDto(String name, UUID id, String text) {
        this.name = name;
        this.id = id;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
