package org.example.model.route;

public enum NodeType {
    SOURCE,         // Источник данных (Kafka topic, REST вход и т.д.)
    VALIDATION,     // Проверка или фильтрация ++
    SPLITTER,       // Разделение сообщений
    AGGREGATOR,     // Сбор сообщений
    FILTER,         // Пропуск по условию
    REST_CALL,      // Вызов REST-сервиса ++
    SOAP_CALL,      // Вызов SOAP-сервиса
    HTTP_CALL,      // Вызов HTTP API ++
    BATCH_BUILDER,  // Сбор сообщений в пачку
    BATCH_SPLITTER, // Разбор пачки
    MERGER,         // Объединение потоков
    SINK,           // Финальный выход маршрута
    BROADCAST,
    LOG,
    API_CALL
}
