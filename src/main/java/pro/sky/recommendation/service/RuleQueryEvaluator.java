package pro.sky.recommendation.service;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.model.RuleQuery;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.CREDIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEBIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEPOSIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.INVEST;
import static pro.sky.recommendation.repository.RecommendationsRepository.SAVING;
import static pro.sky.recommendation.repository.RecommendationsRepository.WITHDRAW;

/**
 * Проверяет и вычисляет отдельные запросы динамического правила ({@link RuleQuery}).
 *
 * <p>Чтобы добавить новый тип запроса, нужно добавить ветку и в {@link #evaluate}, и в {@link #validate}.</p>
 */
@Component
public class RuleQueryEvaluator {

    private static final Set<String> PRODUCT_TYPES = Set.of(DEBIT, CREDIT, INVEST, SAVING);
    private static final Set<String> TRANSACTION_TYPES = Set.of(DEPOSIT, WITHDRAW);
    private static final Set<String> OPERATORS = Set.of(">", "<", "=", ">=", "<=");
    private static final int ACTIVE_USER_MIN_TRANSACTIONS = 5;

    private final RecommendationsRepository repository;

    /**
     * Создаёт вычислитель.
     *
     * @param repository репозиторий основной БД
     */
    public RuleQueryEvaluator(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * Вычисляет запрос для клиента с учётом флага {@code negate}.
     *
     * @param userId идентификатор клиента
     * @param query  запрос правила (уже прошедший {@link #validate})
     * @return результат запроса
     * @throws IllegalArgumentException для неизвестного типа запроса или оператора
     */
    public boolean evaluate(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();

        boolean result = switch (query.getQuery()) {
            case "USER_OF" -> repository.usesProductType(userId, args.get(0));
            case "ACTIVE_USER_OF" ->
                    repository.countTransactions(userId, args.get(0)) >= ACTIVE_USER_MIN_TRANSACTIONS;
            // arguments: [productType, transactionType, operator, constant]
            case "TRANSACTION_SUM_COMPARE" ->
                    compare(repository.sumByTransactionType(userId, args.get(0), args.get(1)),
                            args.get(2),
                            Long.parseLong(args.get(3)));
            // arguments: [productType, operator]; сравниваются сумма пополнений и сумма трат
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" ->
                    compare(repository.sumByTransactionType(userId, args.get(0), DEPOSIT),
                            args.get(1),
                            repository.sumByTransactionType(userId, args.get(0), WITHDRAW));
            default -> throw new IllegalArgumentException("Неизвестный тип запроса: " + query.getQuery());
        };

        return query.isNegate() ? !result : result;
    }

    /**
     * Проверяет корректность запроса при добавлении правила, чтобы в БД не попали некорректные данные.
     *
     * @param query запрос правила
     * @throws IllegalArgumentException если тип запроса неизвестен или аргументы некорректны
     */
    public void validate(RuleQuery query) {
        if (query == null || query.getQuery() == null || query.getArguments() == null) {
            throw new IllegalArgumentException("Каждый запрос должен содержать query и arguments");
        }
        List<String> args = query.getArguments();

        switch (query.getQuery()) {
            case "USER_OF", "ACTIVE_USER_OF" -> {
                requireSize(query, 1);
                requireOneOf(args.get(0), PRODUCT_TYPES, "тип продукта");
            }
            case "TRANSACTION_SUM_COMPARE" -> {
                requireSize(query, 4);
                requireOneOf(args.get(0), PRODUCT_TYPES, "тип продукта");
                requireOneOf(args.get(1), TRANSACTION_TYPES, "тип транзакции");
                requireOneOf(args.get(2), OPERATORS, "оператор сравнения");
                requireNonNegativeInt(args.get(3));
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> {
                requireSize(query, 2);
                requireOneOf(args.get(0), PRODUCT_TYPES, "тип продукта");
                requireOneOf(args.get(1), OPERATORS, "оператор сравнения");
            }
            default -> throw new IllegalArgumentException("Неизвестный тип запроса: " + query.getQuery());
        }
    }

    private boolean compare(long left, String operator, long right) {
        return switch (operator) {
            case ">" -> left > right;
            case "<" -> left < right;
            case "=" -> left == right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> throw new IllegalArgumentException("Неизвестный оператор: " + operator);
        };
    }

    private void requireSize(RuleQuery query, int expected) {
        if (query.getArguments().size() != expected) {
            throw new IllegalArgumentException("Запрос " + query.getQuery()
                    + " принимает аргументов: " + expected);
        }
    }

    private void requireOneOf(String value, Set<String> allowed, String what) {
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException("Недопустимый " + what + ": " + value
                    + ". Допустимо: " + allowed);
        }
    }

    private void requireNonNegativeInt(String value) {
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Константа должна быть целым числом: " + value);
        }
        if (number < 0) {
            throw new IllegalArgumentException("Константа не может быть отрицательной: " + value);
        }
    }
}
