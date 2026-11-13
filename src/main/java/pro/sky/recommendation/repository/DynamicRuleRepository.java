package pro.sky.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.recommendation.model.DynamicRule;

import java.util.UUID;

/**
 * Репозиторий динамических правил (PostgreSQL).
 */
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {

    /**
     * Удаляет все правила продукта.
     *
     * @param productId идентификатор продукта
     * @return число удалённых правил
     */
    long deleteByProductId(UUID productId);
}
