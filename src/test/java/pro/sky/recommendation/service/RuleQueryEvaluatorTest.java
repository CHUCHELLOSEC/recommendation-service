package pro.sky.recommendation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.recommendation.model.RuleQuery;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleQueryEvaluatorTest {

    private static final UUID USER = UUID.randomUUID();

    @Mock
    private RecommendationsRepository repository;

    private RuleQueryEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RuleQueryEvaluator(repository);
    }

    private static RuleQuery query(String type, boolean negate, String... args) {
        RuleQuery query = new RuleQuery();
        query.setQuery(type);
        query.setArguments(List.of(args));
        query.setNegate(negate);
        return query;
    }

    @Test
    void userOfAndItsNegation() {
        when(repository.usesProductType(USER, "DEBIT")).thenReturn(true);

        assertThat(evaluator.evaluate(USER, query("USER_OF", false, "DEBIT"))).isTrue();
        assertThat(evaluator.evaluate(USER, query("USER_OF", true, "DEBIT"))).isFalse();
    }

    @Test
    void activeUserOfNeedsAtLeastFiveTransactions() {
        when(repository.countTransactions(USER, "CREDIT")).thenReturn(4, 5);

        assertThat(evaluator.evaluate(USER, query("ACTIVE_USER_OF", false, "CREDIT"))).isFalse();
        assertThat(evaluator.evaluate(USER, query("ACTIVE_USER_OF", false, "CREDIT"))).isTrue();
    }

    @ParameterizedTest
    @CsvSource({">,true", "<,false", "=,false", ">=,true", "<=,false"})
    void transactionSumCompare(String operator, boolean expected) {
        when(repository.sumByTransactionType(USER, "SAVING", "DEPOSIT")).thenReturn(1500L);

        RuleQuery query = query("TRANSACTION_SUM_COMPARE", false, "SAVING", "DEPOSIT", operator, "1000");

        assertThat(evaluator.evaluate(USER, query)).isEqualTo(expected);
    }

    @Test
    void transactionSumCompareDepositWithdraw() {
        when(repository.sumByTransactionType(USER, "DEBIT", "DEPOSIT")).thenReturn(300L);
        when(repository.sumByTransactionType(USER, "DEBIT", "WITHDRAW")).thenReturn(200L);

        assertThat(evaluator.evaluate(USER,
                query("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW", false, "DEBIT", ">"))).isTrue();
    }

    @Test
    void validateAcceptsCorrectQuery() {
        assertThatCode(() -> evaluator.validate(
                query("TRANSACTION_SUM_COMPARE", false, "DEBIT", "WITHDRAW", ">=", "100000")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateRejectsUnknownQuery() {
        assertThatThrownBy(() -> evaluator.validate(query("UNKNOWN", false, "DEBIT")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateRejectsWrongArgumentCount() {
        assertThatThrownBy(() -> evaluator.validate(query("USER_OF", false, "DEBIT", "CREDIT")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateRejectsUnknownProductType() {
        assertThatThrownBy(() -> evaluator.validate(query("USER_OF", false, "CRYPTO")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateRejectsNegativeConstant() {
        assertThatThrownBy(() -> evaluator.validate(
                query("TRANSACTION_SUM_COMPARE", false, "DEBIT", "DEPOSIT", ">", "-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("отрицательной");
    }
}
