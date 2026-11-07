package pro.sky.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.recommendation.model.DynamicRule;

import java.util.UUID;

public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {

    long deleteByProductId(UUID productId);
}