package pro.sky.recommendation.rules;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.Optional;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.DEBIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEPOSIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.SAVING;
import static pro.sky.recommendation.repository.RecommendationsRepository.WITHDRAW;

/**
 * Статическое правило продукта «Top Saving».
 *
 * <p>Условия (все одновременно):</p>
 * <ol>
 *     <li>клиент пользуется хотя бы одним продуктом типа DEBIT;</li>
 *     <li>сумма пополнений по DEBIT не меньше 50 000 <b>или</b> сумма пополнений по SAVING не меньше 50 000;</li>
 *     <li>сумма пополнений по DEBIT больше суммы трат по DEBIT.</li>
 * </ol>
 */
@Component
public class TopSavingRuleSet implements RecommendationRuleSet {

    private static final UUID ID = UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");
    private static final String NAME = "Top Saving";
    private static final long MIN_DEPOSIT = 50_000;
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

    /**
     * Создаёт правило.
     *
     * @param repository репозиторий основной БД
     */
    public TopSavingRuleSet(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        // Самое дешёвое условие первым: если дебетовых продуктов нет, остальные запросы не нужны
        if (!repository.usesProductType(userId, DEBIT)) {
            return Optional.empty();
        }

        long debitDeposit = repository.sumByTransactionType(userId, DEBIT, DEPOSIT);
        long debitWithdraw = repository.sumByTransactionType(userId, DEBIT, WITHDRAW);

        // Сумма по SAVING запрашивается только если условие по DEBIT не выполнилось (ленивое «ИЛИ»)
        boolean enoughDeposit = debitDeposit >= MIN_DEPOSIT
                || repository.sumByTransactionType(userId, SAVING, DEPOSIT) >= MIN_DEPOSIT;

        if (enoughDeposit && debitDeposit > debitWithdraw) {
            return Optional.of(new RecommendationDto(NAME, ID, TEXT));
        }
        return Optional.empty();
    }
}
