package pro.sky.recommendation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа сервиса рекомендаций банка «Стар».
 *
 * <p>Сервис подбирает клиентам банковские продукты по статическим правилам (зашиты в код)
 * и по динамическим правилам (хранятся в PostgreSQL и управляются через REST API),
 * а также отдаёт рекомендации через Telegram-бота.</p>
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Recommendation Service API",
        version = "0.0.1",
        description = "Рекомендации банковских продуктов, управление динамическими правилами и служебные эндпоинты"))
public class RecommendationServiceApplication {

    /**
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
