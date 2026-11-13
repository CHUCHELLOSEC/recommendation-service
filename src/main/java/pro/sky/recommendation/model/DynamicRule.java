package pro.sky.recommendation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * Динамическое правило рекомендации: продукт и список запросов, которые должны выполниться все.
 * Хранится в таблице {@code dynamic_rule} второй БД; используется и как сущность JPA, и как тело REST API.
 */
@Entity
@Table(name = "dynamic_rule")
public class DynamicRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("product_name")
    @Column(name = "product_name", nullable = false)
    private String productName;

    @JsonProperty("product_id")
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @JsonProperty("product_text")
    @Column(name = "product_text", nullable = false)
    private String productText;

    // Весь массив запросов хранится одной JSONB-колонкой
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule", nullable = false, columnDefinition = "jsonb")
    private List<RuleQuery> rule;

    /**
     * Конструктор для JPA и Jackson.
     */
    public DynamicRule() {
    }

    /**
     * @return идентификатор правила
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id идентификатор правила
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return название продукта
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName название продукта
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return идентификатор продукта
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * @param productId идентификатор продукта
     */
    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    /**
     * @return текст рекомендации
     */
    public String getProductText() {
        return productText;
    }

    /**
     * @param productText текст рекомендации
     */
    public void setProductText(String productText) {
        this.productText = productText;
    }

    /**
     * @return запросы правила, объединённые логическим «И»
     */
    public List<RuleQuery> getRule() {
        return rule;
    }

    /**
     * @param rule запросы правила, объединённые логическим «И»
     */
    public void setRule(List<RuleQuery> rule) {
        this.rule = rule;
    }
}
