# Signal Service

Сервис сигнализации для WebRTC на базе Spring Boot и WebSocket.

## Требования

- Java 21 или выше
- Gradle 8.5 или выше (или используйте Gradle Wrapper)
- Docker и Docker Compose (для запуска через Docker)

## Сборка проекта

### Локальная сборка

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd signal-service
```

2. Соберите проект с помощью Gradle:
```bash
./gradlew clean build
```

3. Запустите приложение:
```bash
./gradlew bootRun
```

Или запустите собранный JAR файл:
```bash
java -jar build/libs/signalling-server-0.0.1-SNAPSHOT.jar
```

Приложение будет доступно по адресу: `http://localhost:8080`

### Сборка через Docker

1. Соберите Docker образ:
```bash
docker build -t signalling-server .
```

2. Запустите контейнер:
```bash
docker run -p 8080:8080 signalling-server
```

### Запуск через Docker Compose

Для удобного запуска с предустановленными настройками используйте Docker Compose:

```bash
docker-compose up -d
```

Это запустит сервис с:
- Порт: `8080`
- Логи в директории `./logs`
- Health check на `/api/health`

Для остановки:
```bash
docker-compose down
```

## Конфигурация

Основные настройки находятся в `src/main/resources/application.properties`:

- **Порт сервера**: `server.port=8080`
- **Логирование**: настраивается через `logging.level.*`
- **Провайдер аутентификации**: `auth.provider.enabled=true/false`
- **Heartbeat**: интервал и таймауты для проверки соединений

Для production окружения используйте `application-production.properties`.

## Проверка работоспособности

После запуска проверьте health endpoint:

```bash
curl http://localhost:8080/api/health
```

## Структура проекта

```
signal-service/
├── src/main/java/ru/itmo/calls/
│   ├── config/          # Конфигурация Spring
│   ├── controller/      # REST контроллеры
│   ├── handler/         # WebSocket обработчики
│   ├── model/           # Модели данных
│   ├── security/        # Провайдеры аутентификации
│   └── service/         # Бизнес-логика
├── src/main/resources/  # Конфигурационные файлы
├── build.gradle.kts     # Конфигурация Gradle
├── Dockerfile           # Docker образ
└── docker-compose.yml   # Docker Compose конфигурация
```

## Разработка

Для запуска в режиме разработки с hot reload:

```bash
./gradlew bootRun
```

При изменении кода приложение автоматически перезапустится (если включен Spring DevTools).

## Тестирование

Запуск тестов:

```bash
./gradlew test
```

## Логи

Логи приложения сохраняются в директории `logs/` (при запуске через Docker Compose) или выводятся в консоль при локальном запуске.


