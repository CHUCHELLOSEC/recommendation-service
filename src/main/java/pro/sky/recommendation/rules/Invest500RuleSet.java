package pro.sky.recommendation.rules;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.Optional;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.*;

@Component
public class Invest500RuleSet implements RecommendationRuleSet {

    private static final UUID ID = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    private static final String NAME = "Invest 500";
    private static final String TEXT = """
            Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! \
            Воспользуйтесь налоговыми льготами и начните инвестировать с умом. \
            Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. \
            Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. \
            Откройте ИИС сегодня и станьте ближе к финансовой независимости!""";

    private final RecommendationsRepository repository;

    public Invest500RuleSet(RecommendationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasDebit = repository.usesProductType(userId, DEBIT);
        boolean hasInvest = repository.usesProductType(userId, INVEST);
        long sumSavingDeposit = repository.sumByTransactionType(userId, SAVING, DEPOSIT);

        if (hasDebit && !hasInvest && sumSavingDeposit > 1000) {
            return Optional.of(new RecommendationDto(NAME, ID, TEXT));
        }
        return Optional.empty();
    }
}
