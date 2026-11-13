package pro.sky.recommendation.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static pro.sky.recommendation.repository.RecommendationsRepository.CREDIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEBIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEPOSIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.INVEST;
import static pro.sky.recommendation.repository.RecommendationsRepository.SAVING;
import static pro.sky.recommendation.repository.RecommendationsRepository.WITHDRAW;

@ExtendWith(MockitoExtension.class)
class StaticRuleSetsTest {

    private static final UUID USER = UUID.randomUUID();

    @Mock
    private RecommendationsRepository repository;

    @Nested
    class Invest500 {

        private Invest500RuleSet rule;

        @BeforeEach
        void setUp() {
            rule = new Invest500RuleSet(repository);
        }

        @Test
        void recommendsWhenAllConditionsMet() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.usesProductType(USER, INVEST)).thenReturn(false);
            when(repository.sumByTransactionType(USER, SAVING, DEPOSIT)).thenReturn(1001L);

            assertThat(rule.check(USER)).hasValueSatisfying(dto ->
                    assertThat(dto.getName()).isEqualTo("Invest 500"));
        }

        @Test
        void doesNotRecommendWhenSavingDepositIsExactly1000() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.usesProductType(USER, INVEST)).thenReturn(false);
            when(repository.sumByTransactionType(USER, SAVING, DEPOSIT)).thenReturn(1000L);

            assertThat(rule.check(USER)).isEmpty();
        }

        @Test
        void doesNotRecommendWhenUserAlreadyInvests() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.usesProductType(USER, INVEST)).thenReturn(true);
            when(repository.sumByTransactionType(USER, SAVING, DEPOSIT)).thenReturn(5000L);

            assertThat(rule.check(USER)).isEmpty();
        }
    }

    @Nested
    class TopSaving {

        private TopSavingRuleSet rule;

        @BeforeEach
        void setUp() {
            rule = new TopSavingRuleSet(repository);
        }

        @Test
        void recommendsWhenDebitDepositIsEnough() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.sumByTransactionType(USER, DEBIT, DEPOSIT)).thenReturn(50_000L);
            when(repository.sumByTransactionType(USER, DEBIT, WITHDRAW)).thenReturn(10_000L);

            assertThat(rule.check(USER)).hasValueSatisfying(dto ->
                    assertThat(dto.getName()).isEqualTo("Top Saving"));
        }

        @Test
        void recommendsWhenSavingDepositIsEnough() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.sumByTransactionType(USER, DEBIT, DEPOSIT)).thenReturn(20_000L);
            when(repository.sumByTransactionType(USER, DEBIT, WITHDRAW)).thenReturn(10_000L);
            when(repository.sumByTransactionType(USER, SAVING, DEPOSIT)).thenReturn(50_000L);

            assertThat(rule.check(USER)).isPresent();
        }

        @Test
        void doesNotRecommendWhenWithdrawNotLessThanDeposit() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(true);
            when(repository.sumByTransactionType(USER, DEBIT, DEPOSIT)).thenReturn(60_000L);
            when(repository.sumByTransactionType(USER, DEBIT, WITHDRAW)).thenReturn(60_000L);

            assertThat(rule.check(USER)).isEmpty();
        }

        @Test
        void doesNotRecommendWithoutDebit() {
            when(repository.usesProductType(USER, DEBIT)).thenReturn(false);

            assertThat(rule.check(USER)).isEmpty();
        }
    }

    @Nested
    class SimpleCredit {

        private SimpleCreditRuleSet rule;

        @BeforeEach
        void setUp() {
            rule = new SimpleCreditRuleSet(repository);
        }

        @Test
        void recommendsWhenAllConditionsMet() {
            when(repository.usesProductType(USER, CREDIT)).thenReturn(false);
            when(repository.sumByTransactionType(USER, DEBIT, DEPOSIT)).thenReturn(200_000L);
            when(repository.sumByTransactionType(USER, DEBIT, WITHDRAW)).thenReturn(100_001L);

            assertThat(rule.check(USER)).hasValueSatisfying(dto ->
                    assertThat(dto.getName()).isEqualTo("Простой кредит"));
        }

        @Test
        void doesNotRecommendWhenUserHasCredit() {
            when(repository.usesProductType(USER, CREDIT)).thenReturn(true);

            assertThat(rule.check(USER)).isEmpty();
        }

        @Test
        void doesNotRecommendWhenWithdrawIsTooSmall() {
            when(repository.usesProductType(USER, CREDIT)).thenReturn(false);
            lenient().when(repository.sumByTransactionType(USER, DEBIT, DEPOSIT)).thenReturn(200_000L);
            when(repository.sumByTransactionType(USER, DEBIT, WITHDRAW)).thenReturn(100_000L);

            assertThat(rule.check(USER)).isEmpty();
        }
    }
}
