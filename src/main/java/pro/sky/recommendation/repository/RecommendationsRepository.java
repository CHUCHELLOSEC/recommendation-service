package pro.sky.recommendation.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.sky.recommendation.dto.BankUser;

import java.util.List;
import java.util.UUID;

/**
 * Единственное место с SQL-запросами к основной БД (H2, только чтение).
 *
 * <p>Результат каждого запроса кешируется в Caffeine: данные о транзакциях меняются редко,
 * а одни и те же значения нужны нескольким правилам подряд. Кеши сбрасываются
 * через {@code POST /management/clear-caches}.</p>
 */
@Repository
public class RecommendationsRepository {

    /** Тип продукта: дебетовый. */
    public static final String DEBIT = "DEBIT";
    /** Тип продукта: кредитный. */
    public static final String CREDIT = "CREDIT";
    /** Тип продукта: инвестиционный. */
    public static final String INVEST = "INVEST";
    /** Тип продукта: сберегательный. */
    public static final String SAVING = "SAVING";

    /** Тип транзакции: пополнение. */
    public static final String DEPOSIT = "DEPOSIT";
    /** Тип транзакции: трата. */
    public static final String WITHDRAW = "WITHDRAW";

    private static final long CACHE_MAX_SIZE = 10_000;

    private record CountKey(UUID userId, String productType) {
    }

    private record SumKey(UUID userId, String productType, String transactionType) {
    }

    private final Cache<CountKey, Integer> countCache = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    private final Cache<SumKey, Long> sumCache = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    private final Cache<String, List<BankUser>> usersCache = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    private final JdbcTemplate jdbcTemplate;

    /**
     * Создаёт репозиторий.
     *
     * @param jdbcTemplate шаблон JDBC для основной БД
     */
    public RecommendationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Число транзакций клиента по продуктам заданного типа.
     *
     * @param userId      идентификатор клиента
     * @param productType тип продукта ({@link #DEBIT}, {@link #CREDIT}, {@link #INVEST}, {@link #SAVING})
     * @return число транзакций
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
     * Пользуется ли клиент продуктом заданного типа (есть хотя бы одна транзакция).
     *
     * @param userId      идентификатор клиента
     * @param productType тип продукта
     * @return {@code true}, если транзакции есть
     */
    public boolean usesProductType(UUID userId, String productType) {
        return countTransactions(userId, productType) > 0;
    }

    /**
     * Сумма транзакций заданного вида по всем продуктам заданного типа.
     *
     * @param userId          идентификатор клиента
     * @param productType     тип продукта
     * @param transactionType тип транзакции ({@link #DEPOSIT} или {@link #WITHDRAW})
     * @return сумма; 0, если транзакций нет
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

    /**
     * Все клиенты с указанным именем пользователя (их может быть 0, 1 или несколько).
     *
     * @param username имя пользователя
     * @return найденные клиенты
     */
    public List<BankUser> findUsersByUsername(String username) {
        return usersCache.get(username, key -> jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM users WHERE username = ?",
                (rs, rowNum) -> new BankUser(
                        rs.getObject("id", UUID.class),
                        rs.getString("first_name"),
                        rs.getString("last_name")),
                key));
    }

    /**
     * Полностью очищает все кеши запросов.
     */
    public void clearCaches() {
        countCache.invalidateAll();
        sumCache.invalidateAll();
        usersCache.invalidateAll();
    }
}
