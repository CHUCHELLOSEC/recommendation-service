package pro.sky.recommendation.dto;

import java.util.UUID;

/**
 * Клиент банка из таблицы users.
 */
public record BankUser(UUID id, String firstName, String lastName) {
}