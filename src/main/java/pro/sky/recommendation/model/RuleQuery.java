package pro.sky.recommendation.model;

import java.util.List;

/**
 * Один запрос динамического правила.
 *
 * <p>Поддерживаемые типы {@code query}:</p>
 * <ul>
 *     <li>{@code USER_OF [productType]}: у клиента есть транзакции по продукту типа;</li>
 *     <li>{@code ACTIVE_USER_OF [productType]}: не меньше 5 транзакций по продукту типа;</li>
 *     <li>{@code TRANSACTION_SUM_COMPARE [productType, transactionType, operator, constant]}:
 *     сумма транзакций сравнивается с константой;</li>
 *     <li>{@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW [productType, operator]}:
 *     сумма пополнений сравнивается с суммой трат.</li>
 * </ul>
 * <p>{@code negate = true} инвертирует результат запроса.</p>
 */
public class RuleQuery {

    private String query;
    private List<String> arguments;
    private boolean negate;

    /**
     * Конструктор для Jackson.
     */
    public RuleQuery() {
    }

    /**
     * @return тип запроса
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query тип запроса
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return аргументы запроса
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * @param arguments аргументы запроса
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * @return {@code true}, если результат запроса нужно инвертировать
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * @param negate {@code true}, если результат запроса нужно инвертировать
     */
    public void setNegate(boolean negate) {
        this.negate = negate;
    }
}
