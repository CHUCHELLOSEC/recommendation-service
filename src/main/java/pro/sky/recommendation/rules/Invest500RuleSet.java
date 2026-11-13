package pro.sky.recommendation.rules;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.Optional;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.DEBIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEPOSIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.INVEST;
import static pro.sky.recommendation.repository.RecommendationsRepository.SAVING;

/**
 * Статическое правило продукта «Invest 500».
 *
 * <p>Условия (все одновременно):</p>
 * <ol>
 *     <li>клиент пользуется хотя бы одним продуктом типа DEBIT;</li>
 *     <li>клиент не пользуется продуктами типа INVEST;</li>
 *     <li>сумма пополнений продуктов типа SAVING больше 1000.</li>
 * </ol>
 */
@Component
public class Invest500RuleSet implements RecommendationRuleSet {

    private static final UUID ID = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    private static final String NAME = "Invest 500";
    private static final long MIN_SAVING_DEPOSIT = 1000;
    private static final String TEXT = """
            Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! \
            Воспользуйтесь налоговыми льготами и начните инвестировать с умом. \
            Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. \
            Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. \
            Откройте ИИС сегодня и станьте ближе к финансовой независимости!""";

    private final RecommendationsRepository repository;

    /**
     * Создаёт правило.
     *
     * @param repository репозиторий основной БД
     */
    public Invest500RuleSet(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasDebit = repository.usesProductType(userId, DEBIT);
        boolean hasInvest = repository.usesProductType(userId, INVEST);
        long sumSavingDeposit = repository.sumByTransactionType(userId, SAVING, DEPOSIT);

        if (hasDebit && !hasInvest && sumSavingDeposit > MIN_SAVING_DEPOSIT) {
            return Optional.of(new RecommendationDto(NAME, ID, TEXT));
        }
        return Optional.empty();
    }
}
