package pro.sky.recommendation.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.recommendation.repository.RecommendationsRepository;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private final RecommendationsRepository repository;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final String fallbackName;

    public ManagementController(RecommendationsRepository repository,
                                ObjectProvider<BuildProperties> buildProperties,
                                @Value("${spring.application.name:recommendation-service}") String fallbackName) {
        this.repository = repository;
        this.buildProperties = buildProperties;
        this.fallbackName = fallbackName;
    }

    /** POST /management/clear-caches: без тела, очищает все кеши. */
    @PostMapping("/clear-caches")
    public void clearCaches() {
        repository.clearCaches();
    }

    /** GET /management/info: название и версия из pom.xml. */
    @GetMapping("/info")
    public ServiceInfo info() {
        BuildProperties properties = buildProperties.getIfAvailable();
        if (properties == null) {
            // Файл build-info ещё не сгенерирован (см. 5.2), поэтому запасные значения
            return new ServiceInfo(fallbackName, "unknown");
        }
        return new ServiceInfo(properties.getArtifact(), properties.getVersion());
    }

    public record ServiceInfo(String name, String version) {
    }
}