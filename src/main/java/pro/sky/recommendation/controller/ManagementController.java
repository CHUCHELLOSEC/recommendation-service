package pro.sky.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.repository.RecommendationsRepository;

/**
 * Служебные эндпоинты для внешних систем: сброс кешей и информация о сборке.
 */
@RestController
@RequestMapping("/management")
@Tag(name = "Management", description = "Служебные операции для внешних систем")
public class ManagementController {

    private final RecommendationsRepository repository;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final String fallbackName;

    /**
     * Создаёт контроллер.
     *
     * @param repository      репозиторий основной БД, владеющий кешами
     * @param buildProperties данные о сборке из {@code META-INF/build-info.properties}, если файл есть
     * @param fallbackName    имя сервиса на случай, когда файла build-info нет (запуск из IDE)
     */
    public ManagementController(RecommendationsRepository repository,
                                ObjectProvider<BuildProperties> buildProperties,
                                @Value("${spring.application.name:recommendation-service}") String fallbackName) {
        this.repository = repository;
        this.buildProperties = buildProperties;
        this.fallbackName = fallbackName;
    }

    /**
     * {@code POST /management/clear-caches}: очищает все кеши запросов к основной БД.
     */
    @PostMapping("/clear-caches")
    @Operation(summary = "Сбросить все кеши запросов к основной БД")
    public void clearCaches() {
        repository.clearCaches();
    }

    /**
     * {@code GET /management/info}: название и версия сервиса из {@code pom.xml}.
     *
     * @return имя артефакта и версия
     */
    @GetMapping("/info")
    @Operation(summary = "Получить имя и версию сервиса")
    public ServiceInfo info() {
        BuildProperties properties = buildProperties.getIfAvailable();
        if (properties == null) {
            // build-info создаётся только при сборке Maven; при запуске из IDE его может не быть
            return new ServiceInfo(fallbackName, "unknown");
        }
        return new ServiceInfo(properties.getArtifact(), properties.getVersion());
    }

    /**
     * Ответ {@code GET /management/info}.
     *
     * @param name    имя сервиса (artifactId)
     * @param version версия сервиса
     */
    public record ServiceInfo(String name, String version) {
    }
}
