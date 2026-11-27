# Система расчёта зарплаты и управления сотрудниками

> Курсовой проект: десктопное клиент-серверное приложение на **Java 21 + JavaFX**, предназначенное для учёта сотрудников, расчёта зарплаты, управления штрафами и внутренними уведомлениями.

---

## Содержание

- [Возможности](#возможности)
- [Роли пользователей](#роли-пользователей)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Технологии](#технологии)
- [Требования](#требования)
- [Установка и настройка](#установка-и-настройка)
- [Запуск](#запуск)
- [База данных](#база-данных)
- [Сетевой протокол](#сетевой-протокол)
- [Скриншоты](#скриншоты)
- [Известные ограничения](#известные-ограничения)
- [Автор](#автор)
- [Лицензия](#лицензия)

---

## Возможности

### Для администратора

- просмотр полного списка сотрудников;
- добавление новых сотрудников (ФИО, телефон, должность, оклад, дата найма, отдел);
- поиск сотрудника по полному имени;
- удаление сотрудника по логину;
- расчёт зарплаты по формуле и сохранение результата в базу данных;
- проверка существования должности перед добавлением начисления;
- просмотр, добавление и удаление штрафов;
- отправка сообщений/уведомлений сотрудникам;
- просмотр всех сообщений и штрафов;
- экспорт таблиц в текстовые файлы: зарплаты, штрафы, сообщения.

### Для пользователя

- вход в систему и регистрация (регистрация привязана к существующему сотруднику по ФИО);
- просмотр своего профиля и личных данных;
- редактирование ФИО, номера телефона, логина и пароля;
- расчёт зарплаты в валютах BYN / USD / RUB;
- просмотр графика динамики зарплаты;
- просмотр своих штрафов;
- просмотр входящих уведомлений.

### Формула расчёта зарплаты

```text
gross = базовая ставка + (отработанные часы × почасовая ставка) − штрафы + премия
net   = gross × 0.87
```

Дополнительно результат округляется до 2 знаков и конвертируется:

- BYN → USD: деление на `3.1`;
- BYN → RUB: умножение на `30`.

---

## Роли пользователей

Роль определяется **отделом сотрудника** при регистрации:

| Отдел (DepartmentName) | Роль      | Возможности                         |
|------------------------|-----------|-------------------------------------|
| `it`                   | `Admin`   | полный доступ к управлению          |
| любой другой           | `User`    | доступ только к своим данным        |

Администратор по умолчанию создаётся автоматически при первом подключении к базе данных на основе переменных из файла `.env`.

---

## Архитектура

Приложение построено по архитектуре **клиент — сервер — база данных**:

```text
┌────────────────────────┐        TCP/IP, JSON        ┌──────────────────────────┐
│  JavaFX клиент         │  ────────────────────────►  │  Сервер (ServerSocket)   │
│  Pages + ClientContext │  ◄────────────────────────  │  ClientHandler (пул)     │
│  NetworkClient         │                             │  RequestDispatcher       │
└────────────────────────┘                             │  Services → JDBC         │
                                                      └────────────┬─────────────┘
                                                                   │
                                                      ┌────────────▼─────────────┐
                                                      │ PostgreSQL salarysystem  │
                                                      └──────────────────────────┘
```

### Основные слои

| Слой | Пакет | Назначение |
|------|-------|------------|
| UI | [`Pages`](k1/src/main/java/Pages) | FXML-страницы и контроллеры JavaFX |
| Клиент | [`client`](k1/src/main/java/client) | единый `NetworkClient` на сессию, точка входа `ClientApp` |
| Протокол | [`protocol`](k1/src/main/java/protocol) | `Request`, `Response`, `JsonCodec`, `NetworkClient` |
| Сервер | [`server`](k1/src/main/java/server) | `Server`, `ClientHandler`, `RequestDispatcher` |
| Сервисы | [`server.services`](k1/src/main/java/server/services) | бизнес-логика и SQL-запросы |
| База данных | [`Database`](k1/src/main/java/Database) | подключение, инициализация схемы и администратора |
| Конфигурация | [`Config`](k1/src/main/java/Config) | загрузка переменных из `.env`, system properties и env vars |

### Особенности реализации

- **Один `NetworkClient` на клиентскую сессию** — через [`ClientContext`](k1/src/main/java/client/ClientContext.java).
- **Пул потоков сервера** — 10 потоков для обработки клиентов в [`Server`](k1/src/main/java/server/Server.java).
- **Dispatcher вместо `if/else`** — все действия зарегистрированы в `Map<String, RequestHandler>` в [`RequestDispatcher`](k1/src/main/java/server/RequestDispatcher.java).
- **Свой минималистичный JSON-кодек** — без внешних JSON-библиотек, [`JsonCodec`](k1/src/main/java/protocol/JsonCodec.java).
- **Автоматическая инициализация БД** — база, таблицы и администратор создаются при первом подключении.
- **Транзакции** — используются при регистрации, удалении сотрудника и смене логина.
- **Модульность JPMS** — описано в [`module-info.java`](k1/src/main/java/module-info.java).

---

## Структура проекта

```text
k1/
├── pom.xml                          # Maven-конфигурация и зависимости
├── .env                             # Локальные секреты (не коммитится)
├── .gitignore
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   ├── client/
    │   │   ├── ClientApp.java       # Точка входа JavaFX-приложения
    │   │   └── ClientContext.java   # Единый клиент на сессию
    │   ├── Config/
    │   │   └── EnvConfig.java       # Чтение .env / env vars / system properties
    │   ├── Database/
    │   │   ├── Connector.java
    │   │   ├── DatabaseInitializer.java
    │   │   ├── CreateAdminUser.java
    │   │   └── TestDatabaseConnect.java
    │   ├── Pages/
    │   │   ├── LoginPage.java
    │   │   ├── RegPage.java
    │   │   ├── MainAdminPage.java
    │   │   ├── MainUserPage.java
    │   │   ├── AddEmployee.java
    │   │   ├── Employees.java
    │   │   ├── SearchEmployee.java
    │   │   ├── CalculateSalaryAdminPage.java
    │   │   ├── CalculateSalaryUserPage.java
    │   │   ├── GraphPage.java
    │   │   ├── PenaltiesPage.java
    │   │   ├── ShowPenaltiesPage.java
    │   │   ├── UserTicketsPage.java
    │   │   ├── MessagePage.java
    │   │   ├── UserNotifications.java
    │   │   ├── UserEditInfoPage.java
    │   │   ├── UserStatsPage.java
    │   │   ├── HelpPage.java
    │   │   └── Main.java
    │   ├── protocol/
    │   │   ├── Request.java
    │   │   ├── Response.java
    │   │   ├── JsonCodec.java
    │   │   └── NetworkClient.java
    │   └── server/
    │       ├── Server.java
    │       ├── ClientHandler.java
    │       ├── RequestDispatcher.java
    │       └── services/
    │           ├── UserService.java
    │           ├── EmployeeService.java
    │           ├── SalaryService.java
    │           ├── PenaltyService.java
    │           └── MessageService.java
    └── resources/
        ├── db/init.sql              # SQL-схема базы данных
        ├── Pages/*.fxml             # Разметка интерфейса
        ├── icons/                   # Иконки интерфейса
        └── files/                   # Экспортируемые таблицы (*.txt)
```

---

## Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 | язык программирования, JPMS |
| JavaFX | 21.0.2 | графический интерфейс |
| FXML | — | declarative UI |
| Maven | — | сборка и управление зависимостями |
| PostgreSQL JDBC Driver | 42.7.14 | работа с БД |
| PostgreSQL | — | сервер базы данных |
| JUnit 5 | 5.9.2 | тестирование |
| Sockets + JSON | — | сетевое взаимодействие |

---

## Требования

- **JDK 21** или новее;
- **Maven 3.8+**;
- **PostgreSQL 14+** (локальный или удалённый сервер);
- IDEA / Eclipse / VS Code — на выбор;
- свободный порт **12345** для сервера.

---

## Установка и настройка

### 1. Клонирование репозитория

```bash
git clone <URL_ВАШЕГО_РЕПОЗИТОРИЯ>
cd kursach_2023/k1
```

### 2. Создание файла `.env`

В каталоге `k1/` создайте файл `.env` со следующим содержимым:

```env
# Подключение к PostgreSQL (указывается сервер БД, без имени базы)
DEFAULT_URL=jdbc:postgresql://localhost:5432
DEFAULT_USER=postgres
DEFAULT_PASSWORD=your_password

# Администратор системы (создаётся автоматически)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
ADMIN_ROLE=Admin
```

> Файл `.env` уже добавлен в [`.gitignore`](k1/.gitignore) и **не должен попадать в GitHub**.

Приложение ищет `.env` в таких местах:

1. `.env` в рабочей директории;
2. `target/classes/.env`;
3. `src/main/resources/.env`;
4. classpath-ресурсы `.env`, `env`, `config/.env`.

Приоритет имеют system properties и переменные окружения с теми же именами.

### 3. Подготовка PostgreSQL

Убедитесь, что пользователь из `DEFAULT_USER` может создавать базы данных. База `salarysystem` и все таблицы будут созданы автоматически при первом запуске сервера.

При желании схему можно применить вручную:

```bash
psql -U postgres -f src/main/resources/db/init.sql
```

### 4. Установка зависимостей

```bash
mvn clean install -DskipTests
```

---

## Запуск

### Вариант A — через Maven (рекомендуется)

**Терминал 1 — сервер:**

```bash
cd k1
mvn exec:java
```

Ожидаемый вывод:

```text
Server started on port 12345 (N actions registered)
Database initialization completed.
```

**Терминал 2 — клиент:**

```bash
cd k1
mvn javafx:run
```

### Вариант B — через IntelliJ IDEA

1. Откройте проект `k1` как Maven-проект.
2. Запустите `server.Server.main()` — сервер.
3. Запустите `client.ClientApp.main()` — клиент.
4. При необходимости в VM options укажите:
   ```text
   --enable-native-access=javafx.graphics
   ```
   и рабочую директорию `k1`.

> Важно: сервер должен быть запущен **до** клиента.

---

## База данных

Имя базы: `salarysystem`.

| Таблица | Назначение |
|---------|------------|
| `users` | пользователи системы: логин, пароль, роль |
| `employees` | сотрудники: ФИО, телефон, должность, оклад, дата найма, отдел, логин |
| `positions` | справочник должностей |
| `salaries` | начисления зарплаты и итоговая сумма |
| `penalties` | штрафы сотрудников |
| `messages` | внутренние сообщения/уведомления |

Схема определена в [`init.sql`](k1/src/main/resources/db/init.sql) и дублируется в [`DatabaseInitializer`](k1/src/main/java/Database/DatabaseInitializer.java) для автоматического создания при старте.

---

## Сетевой протокол

Обмен идёт по TCP, одна строка = один JSON-объект, кодировка **UTF-8**.

### Запрос

```json
{
  "action": "LOGIN",
  "payload": {
    "username": "admin",
    "password": "admin123"
  }
}
```

### Ответ

```json
{
  "ok": true,
  "data": "Admin",
  "error": ""
}
```

### Основные действия

| Группа | Действия |
|--------|----------|
| Пользователи | `LOGIN`, `REGISTER`, `CHANGE_USERNAME`, `GET_PASSWORD`, `CHANGE_PASSWORD`, `USER_EXISTS` |
| Сотрудники | `GET_EMPLOYEES`, `ADD_EMPLOYEE`, `SEARCH_EMPLOYEE`, `DELETE_EMPLOYEE`, `DELETE_EMPLOYEE_BY_USERNAME`, `UPDATE_EMPLOYEE_FIELD`, `EMPLOYEE_PROFILE` |
| Зарплата | `POSITION_EXISTS`, `ADD_SALARY`, `GET_SALARIES`, `SALARY_CHART` |
| Штрафы | `GET_PENALTIES`, `GET_ALL_PENALTIES`, `PENALTY_EXISTS`, `ADD_PENALTY`, `DELETE_PENALTIES` |
| Сообщения | `GET_MESSAGES`, `GET_ALL_MESSAGES`, `SEND_MESSAGE`, `DELETE_MESSAGES` |
| Прочее | `PING` |

Настройки подключения клиента заданы в [`NetworkClient`](k1/src/main/java/protocol/NetworkClient.java): `localhost:12345`, таймаут 15 секунд.

---

## Скриншоты

> Раздел зарезервирован. Добавьте screenshots в папку `docs/screenshots/` и вставьте изображения здесь, например:

```markdown
![Главная страница администратора](docs/screenshots/admin-main.png)
![Расчёт зарплаты](docs/screenshots/calculate-salary.png)
```

---

## Известные ограничения

- Пароли хранятся в базе в открытом виде — решение учебное, для продакшена требуется хеширование (BCrypt/Argon2).
- Сессионные токены не используются; клиент передаёт логин в запросах.
- Экспорт в файлы выполняется на стороне клиента по пути `src/main/resources/files/...`.
- Некоторые SQL-строки в инициализаторе формируются конкатенацией — для учебного проекта допустимо, но требует параметризации в реальной системе.
- Приложение рассчитано на локальный запуск; для работы в сети нужно заменить `localhost` в `NetworkClient` на адрес сервера.

---

## Автор

**Чернушевич** — студенческая курсовая работа, 2023–2026 гг.

Отчётная документация расположена в корне репозитория: [`Чернушевич124402.pdf`](Чернушевич124402.pdf), [`Чернушевич124402.docx`](Чернушевич124402.docx).

---

## Лицензия

Проект создан в учебных целях. Использование и модификация разрешены Студентам и преподавателям в рамках учебного процесса.
