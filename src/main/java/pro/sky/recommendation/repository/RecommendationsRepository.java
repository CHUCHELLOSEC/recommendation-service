package pro.sky.recommendation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Единственное место, где есть SQL.
 * Два общих запроса с параметрами обслуживают все правила рекомендаций.
 */
@Repository
public class RecommendationsRepository {

    // --- Типы продуктов (значения колонки type в таблице products) ---
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String INVEST = "INVEST";
    public static final String SAVING = "SAVING";

    // --- Типы транзакций (значения колонки type в таблице transactions) ---
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";

    private final JdbcTemplate jdbcTemplate;

    // JdbcTemplate приходит через конструктор: Spring создаёт его сам из настроек datasource
    public RecommendationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Использует ли пользователь хотя бы один продукт этого типа?
     * Пользователь использует продукт, если по нему есть любая транзакция.
     */
    public boolean usesProductType(UUID userId, String productType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) "
                        + "FROM transactions t "
                        + "JOIN products p ON t.product_id = p.id "
                        + "WHERE t.user_id = ? AND p.type = ?",
                Integer.class,
                userId, productType);
        return count != null && count > 0;
    }

    /**
     * Сумма транзакций заданного вида по всем продуктам заданного типа.
     * Например: сумма пополнений (DEPOSIT) по продуктам DEBIT.
     * COALESCE превращает NULL в 0, если подходящих транзакций нет.
     */
    public long sumByTransactionType(UUID userId, String productType, String transactionType) {
        Long sum = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(t.amount), 0) "
                        + "FROM transactions t "
                        + "JOIN products p ON t.product_id = p.id "
                        + "WHERE t.user_id = ? AND p.type = ? AND t.type = ?",
                Long.class,
                userId, productType, transactionType);
        return sum == null ? 0 : sum;
    }
}
//test//test