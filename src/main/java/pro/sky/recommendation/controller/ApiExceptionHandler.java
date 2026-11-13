package pro.sky.recommendation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Преобразует ошибки валидации в ответ {@code 400 Bad Request} с телом {@code {"error": "..."}}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Обрабатывает некорректные входные данные (например, неизвестный тип запроса в правиле).
     *
     * @param e исключение с описанием ошибки
     * @return тело ответа с текстом ошибки
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
