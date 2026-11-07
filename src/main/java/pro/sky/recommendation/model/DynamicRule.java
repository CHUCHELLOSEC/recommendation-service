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

    public DynamicRule() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductText() { return productText; }
    public void setProductText(String productText) { this.productText = productText; }

    public List<RuleQuery> getRule() { return rule; }
    public void setRule(List<RuleQuery> rule) { this.rule = rule; }
}