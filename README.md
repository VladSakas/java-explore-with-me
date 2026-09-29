# Explore With Me

Дипломный проект: афиша событий, где пользователи делятся мероприятиями и находят компанию.

## Сервисы

- **stats-server** (порт 9090) — сервис статистики
- **ewm-main-service** (порт 8080) — основной сервис
- **stats-client** — HTTP-клиент для работы со статистикой
- **common** — общие DTO

## Стек

- Java 21
- Spring Boot 3.3.2
- PostgreSQL 16.1
- Maven (многомодульный)
- Docker / Docker Compose

## Запуск

Сборка jar-файлов:

    mvn clean package -DskipTests

Запуск всех сервисов через Docker Compose:

    docker-compose up --build

После запуска:

- Основной сервис: http://localhost:8080
- Сервис статистики: http://localhost:9090

Остановка:

    docker-compose down

Остановка с удалением данных БД:

    docker-compose down -v

## API

### Public API (без авторизации)

- `GET /events` — поиск событий с фильтрами
- `GET /events/{id}` — детали события
- `GET /categories` — список категорий
- `GET /categories/{catId}` — одна категория
- `GET /compilations` — список подборок
- `GET /compilations/{compId}` — одна подборка

### Private API (авторизованные)

- `POST /users/{userId}/events` — создать событие
- `GET /users/{userId}/events` — мои события
- `GET /users/{userId}/events/{eventId}` — детали своего события
- `PATCH /users/{userId}/events/{eventId}` — редактировать событие
- `GET /users/{userId}/events/{eventId}/requests` — заявки на моё событие
- `PATCH /users/{userId}/events/{eventId}/requests` — подтвердить/отклонить заявки
- `POST /users/{userId}/requests?eventId={eventId}` — подать заявку на участие
- `GET /users/{userId}/requests` — мои заявки
- `PATCH /users/{userId}/requests/{requestId}/cancel` — отменить заявку

### Admin API (администраторы)

- `POST /admin/users` — создать пользователя
- `GET /admin/users` — список пользователей
- `DELETE /admin/users/{userId}` — удалить пользователя
- `POST /admin/categories` — создать категорию
- `PATCH /admin/categories/{catId}` — обновить категорию
- `DELETE /admin/categories/{catId}` — удалить категорию
- `POST /admin/compilations` — создать подборку
- `PATCH /admin/compilations/{compId}` — обновить подборку
- `DELETE /admin/compilations/{compId}` — удалить подборку
- `GET /admin/events` — поиск событий с фильтрами
- `PATCH /admin/events/{eventId}` — редактировать / публиковать / отклонять событие

## Архитектура

Проект состоит из четырёх модулей:

- **common** — общие DTO (`EndpointHit`, `ViewStats`) для `stats-server` и `stats-client`
- **stats-server** — Spring Boot приложение: REST-контроллер + JPA + PostgreSQL
- **stats-client** — библиотека с `StatsClient` для HTTP-запросов к сервису статистики
- **ewm-main-service** — основной сервис: Admin, Private, Public API + интеграция со статистикой

Основной сервис при каждом публичном запросе к событиям:

1. Сохраняет `hit` в `stats-server` (`POST /hit`).
2. Запрашивает `views` из `stats-server` (`GET /stats`) для формирования `EventFullDto` и `EventShortDto`.

## Дополнительная функциональность

Выбрана фича: **Рейтинги событий и их авторов**.

## Этапы

- [x] Этап 1. Сервис статистики
- [x] Этап 2. Основной сервис
- [ ] Этап 3. Дополнительная функциональность