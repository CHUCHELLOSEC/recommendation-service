package pro.sky.recommendation.rules;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.Optional;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.*;

@Component
public class TopSavingRuleSet implements RecommendationRuleSet {

    private static final UUID ID = UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");
    private static final String NAME = "Top Saving";
    private static final String TEXT = """
            Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, \
            который поможет вам легко и удобно накапливать деньги на важные цели. \
            Больше никаких забытых чеков и потерянных квитанций — всё под контролем!

            Преимущества «Копилки»:

            Накопление средств на конкретные цели. Установите лимит и срок накопления, \
            и банк будет автоматически переводить определенную сумму на ваш счет.

            Прозрачность и контроль. Отслеживайте свои доходы и расходы, \
            контролируйте процесс накопления и корректируйте стратегию при необходимости.

            Безопасность и надежность. Ваши средства находятся под защитой банка, \
            а доступ к ним возможен только через мобильное приложение или интернет-банкинг.

            Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!""";

    private final RecommendationsRepository repository;

    public TopSavingRuleSet(RecommendationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        // Самое дешёвое условие первым: если дебетовых продуктов нет, остальные запросы не нужны
        if (!repository.usesProductType(userId, DEBIT)) {
            return Optional.empty();
        }

        long debitDeposit = repository.sumByTransactionType(userId, DEBIT, DEPOSIT);
        long debitWithdraw = repository.sumByTransactionType(userId, DEBIT, WITHDRAW);

        boolean enoughOnDebit = debitDeposit >= 50_000;
        // Второй запрос выполнится только если первое условие в «ИЛИ» не сработало
        boolean enough = enoughOnDebit
                || repository.sumByTransactionType(userId, SAVING, DEPOSIT) >= 50_000;

        if (enough && debitDeposit > debitWithdraw) {
            return Optional.of(new RecommendationDto(NAME, ID, TEXT));
        }
        return Optional.empty();
    }
}
