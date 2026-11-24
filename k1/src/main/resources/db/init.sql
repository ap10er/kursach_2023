
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'salarysystem') THEN
        CREATE DATABASE salarysystem ENCODING 'UTF8';
    END IF;
END
$$;

-- Переподключаемся к созданной базе (в psql: \connect salarysystem)
\connect salarysystem

-- Пользователи системы
CREATE TABLE IF NOT EXISTS users (
    userid   SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL
);

-- Сотрудники
CREATE TABLE IF NOT EXISTS employees (
    employeeid     SERIAL PRIMARY KEY,
    lastname       VARCHAR(100),
    firstname      VARCHAR(100),
    middlename     VARCHAR(100),
    number         VARCHAR(50),
    position       VARCHAR(100),
    salary         DOUBLE PRECISION,
    hiredate       DATE,
    username       VARCHAR(100),
    departmentname VARCHAR(100)
);

-- Должности
CREATE TABLE IF NOT EXISTS positions (
    positionid   SERIAL PRIMARY KEY,
    positionname VARCHAR(100) NOT NULL UNIQUE
);

-- Начисления зарплаты
CREATE TABLE IF NOT EXISTS salaries (
    salaryid      SERIAL PRIMARY KEY,
    positionname  VARCHAR(100),
    baserate      DOUBLE PRECISION,
    bonus         DOUBLE PRECISION,
    workedhours   INTEGER,
    rateperhour   DOUBLE PRECISION,
    effectivedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result        DOUBLE PRECISION
);

-- Штрафы
CREATE TABLE IF NOT EXISTS penalties (
    penaltyid   SERIAL PRIMARY KEY,
    username    VARCHAR(100),
    fineamount  NUMERIC(12, 2),
    penaltydate DATE DEFAULT CURRENT_DATE,
    description TEXT
);

-- Сообщения / уведомления
CREATE TABLE IF NOT EXISTS messages (
    messageid SERIAL PRIMARY KEY,
    sender    VARCHAR(100),
    username  VARCHAR(100),
    message   TEXT
);

-- Начальные данные (администратор по умолчанию, логин admin / пароль admin)
-- Роль именно 'Admin' — её проверяет LoginPage для открытия меню админа.
INSERT INTO users (username, password, role)
SELECT 'admin', 'admin', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
