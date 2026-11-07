package pro.sky.recommendation.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Единственное место, где есть SQL к основной базе (H2).
 * Два метода с запросами, у каждого свой кеш Caffeine.
 */
@Repository
public class RecommendationsRepository {

    // Типы продуктов
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String INVEST = "INVEST";
    public static final String SAVING = "SAVING";

    // Типы транзакций
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";

    // Составные ключи кешей: по всем параметрам запроса
    private record CountKey(UUID userId, String productType) {
    }

    private record SumKey(UUID userId, String productType, String transactionType) {
    }

    private final Cache<CountKey, Integer> countCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    private final Cache<SumKey, Long> sumCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    private final JdbcTemplate jdbcTemplate;

    public RecommendationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Сколько транзакций у пользователя по продуктам заданного типа.
     * Кеш: get-if-absent-compute. Если ключа нет, SQL выполняется и результат кладётся в кеш.
     */
    public int countTransactions(UUID userId, String productType) {
        return countCache.get(new CountKey(userId, productType), key -> {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) "
                            + "FROM transactions t "
                            + "JOIN products p ON t.product_id = p.id "
                            + "WHERE t.user_id = ? AND p.type = ?",
                    Integer.class,
                    key.userId(), key.productType());
            return count == null ? 0 : count;
        });
    }

    /**
     * Использует ли пользователь продукт этого типа (есть хотя бы одна транзакция).
     * Отдельного запроса и кеша нет: переиспользуем countTransactions.
     */
    public boolean usesProductType(UUID userId, String productType) {
        return countTransactions(userId, productType) > 0;
    }

    /**
     * Сумма транзакций заданного вида по всем продуктам заданного типа.
     */
    public long sumByTransactionType(UUID userId, String productType, String transactionType) {
        return sumCache.get(new SumKey(userId, productType, transactionType), key -> {
            Long sum = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(t.amount), 0) "
                            + "FROM transactions t "
                            + "JOIN products p ON t.product_id = p.id "
                            + "WHERE t.user_id = ? AND p.type = ? AND t.type = ?",
                    Long.class,
                    key.userId(), key.productType(), key.transactionType());
            return sum == null ? 0L : sum;
        });
    }
}