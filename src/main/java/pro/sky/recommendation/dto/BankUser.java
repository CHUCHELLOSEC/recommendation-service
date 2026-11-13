package pro.sky.recommendation.dto;

import java.util.UUID;

/**
 * Клиент банка из таблицы {@code users} основной БД.
 *
 * @param id        идентификатор клиента
 * @param firstName имя
 * @param lastName  фамилия
 */
public record BankUser(UUID id, String firstName, String lastName) {
}
