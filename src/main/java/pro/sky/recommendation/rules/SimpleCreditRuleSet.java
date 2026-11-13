package pro.sky.recommendation.rules;

import org.springframework.stereotype.Component;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.repository.RecommendationsRepository;

import java.util.Optional;
import java.util.UUID;

import static pro.sky.recommendation.repository.RecommendationsRepository.CREDIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEBIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.DEPOSIT;
import static pro.sky.recommendation.repository.RecommendationsRepository.WITHDRAW;

/**
 * Статическое правило продукта «Простой кредит».
 *
 * <p>Условия (все одновременно):</p>
 * <ol>
 *     <li>клиент не пользуется продуктами типа CREDIT;</li>
 *     <li>сумма пополнений по DEBIT больше суммы трат по DEBIT;</li>
 *     <li>сумма трат по DEBIT больше 100 000.</li>
 * </ol>
 */
@Component
public class SimpleCreditRuleSet implements RecommendationRuleSet {

    private static final UUID ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");
    private static final String NAME = "Простой кредит";
    private static final long MIN_DEBIT_WITHDRAW = 100_000;
    private static final String TEXT = """
            Откройте мир выгодных кредитов с нами!

            Ищете способ быстро и без лишних хлопот получить нужную сумму? \
            Тогда наш выгодный кредит — именно то, что вам нужно! \
            Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.

            Почему выбирают нас:

            Быстрое рассмотрение заявки. Мы ценим ваше время, \
            поэтому процесс рассмотрения заявки занимает всего несколько часов.

            Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.

            Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: \
            покупку недвижимости, автомобиля, образование, лечение и многое другое.

            Не упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!""";

    private final RecommendationsRepository repository;

    /**
     * Создаёт правило.
     *
     * @param repository репозиторий основной БД
     */
    public SimpleCreditRuleSet(RecommendationsRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        // У клиента уже есть кредит: остальные запросы не нужны
        if (repository.usesProductType(userId, CREDIT)) {
            return Optional.empty();
        }

        long debitDeposit = repository.sumByTransactionType(userId, DEBIT, DEPOSIT);
        long debitWithdraw = repository.sumByTransactionType(userId, DEBIT, WITHDRAW);

        if (debitDeposit > debitWithdraw && debitWithdraw > MIN_DEBIT_WITHDRAW) {
            return Optional.of(new RecommendationDto(NAME, ID, TEXT));
        }
        return Optional.empty();
    }
}
