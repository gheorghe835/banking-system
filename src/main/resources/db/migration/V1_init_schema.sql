-- =============================================
-- BANCĂ COMERCIALĂ - SCHEMA DE BAZĂ DE DATE
-- Versiune: 1.0
-- Data creării: 2026
-- =============================================

-- Tabela pentru clienți
CREATE TABLE IF NOT EXISTS customers (
                                         customer_id VARCHAR(20) PRIMARY KEY,
                                         first_name VARCHAR(50) NOT NULL,
                                         last_name VARCHAR(50) NOT NULL,
                                         email VARCHAR(100) NOT NULL UNIQUE,
                                         phone_number VARCHAR(20),
                                         birth_date DATE,
                                         address VARCHAR(200),
                                         identity_number VARCHAR(20) UNIQUE,
                                         registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Indexuri pentru performanță
                                         INDEX idx_customer_email (email),
                                         INDEX idx_customer_name (first_name, last_name),
                                         INDEX idx_customer_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru conturi bancare
CREATE TABLE IF NOT EXISTS accounts (
                                        account_number VARCHAR(16) PRIMARY KEY,
                                        customer_id VARCHAR(20) NOT NULL,
                                        account_type VARCHAR(20) NOT NULL, -- 'CURRENT', 'SAVINGS', 'BUSINESS'

    -- Solduri în diferite valute
                                        balance_mdl DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                        balance_eur DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                        balance_usd DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                        balance_gbp DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                                        balance_ron DECIMAL(15,2) NOT NULL DEFAULT 0.00,

    -- Date administrative
                                        creation_date DATE NOT NULL,
                                        last_login TIMESTAMP NULL,
                                        is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Limită retragere zilnică
                                        daily_withdrawal_limit DECIMAL(10,2) NOT NULL DEFAULT 5000.00,
                                        daily_withdrawal_used DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                        last_reset_date DATE NOT NULL,

    -- Cheie străină către customers
                                        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                                            ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Indexuri pentru performanță
                                        INDEX idx_account_customer (customer_id),
                                        INDEX idx_account_type (account_type),
                                        INDEX idx_account_active (is_active),
                                        INDEX idx_account_creation (creation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru tranzacții bancare
CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id VARCHAR(20) PRIMARY KEY,
                                            source_account_number VARCHAR(16),
                                            target_account_number VARCHAR(16),
                                            transaction_type VARCHAR(30) NOT NULL, -- 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', etc.
                                            amount DECIMAL(15,2) NOT NULL,
                                            currency VARCHAR(3) NOT NULL, -- 'MDL', 'EUR', 'USD', 'GBP', 'RON'
                                            description VARCHAR(200),
                                            timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'

    -- Chei străine către accounts
                                            FOREIGN KEY (source_account_number) REFERENCES accounts(account_number)
                                                ON DELETE SET NULL ON UPDATE CASCADE,
                                            FOREIGN KEY (target_account_number) REFERENCES accounts(account_number)
                                                ON DELETE SET NULL ON UPDATE CASCADE,

    -- Indexuri pentru performanță și interogări frecvente
                                            INDEX idx_transaction_source (source_account_number),
                                            INDEX idx_transaction_target (target_account_number),
                                            INDEX idx_transaction_type (transaction_type),
                                            INDEX idx_transaction_timestamp (timestamp),
                                            INDEX idx_transaction_status (status),
                                            INDEX idx_transaction_currency (currency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru manageri bancari
CREATE TABLE IF NOT EXISTS bank_managers (
                                             employee_id VARCHAR(20) PRIMARY KEY,
                                             username VARCHAR(50) NOT NULL UNIQUE,
                                             password_hash VARCHAR(255) NOT NULL,
                                             first_name VARCHAR(50) NOT NULL,
                                             last_name VARCHAR(50) NOT NULL,
                                             email VARCHAR(100) NOT NULL UNIQUE,
                                             department VARCHAR(50),
                                             is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                             access_level VARCHAR(20) NOT NULL, -- 'JUNIOR', 'SENIOR', 'ADMIN', 'SUPER_ADMIN'
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             last_login TIMESTAMP NULL,

    -- Indexuri
                                             INDEX idx_manager_username (username),
                                             INDEX idx_manager_email (email),
                                             INDEX idx_manager_active (is_active),
                                             INDEX idx_manager_access (access_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru ratele de schimb valutar
CREATE TABLE IF NOT EXISTS exchange_rates (
                                              currency_code VARCHAR(3) PRIMARY KEY, -- 'EUR', 'USD', 'GBP', 'RON'
                                              rate_to_mdl DECIMAL(10,4) NOT NULL,
                                              last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              daily_change DECIMAL(5,4) DEFAULT 0.0000,
                                              description VARCHAR(100),

                                              INDEX idx_exchange_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru istoric rate de schimb
CREATE TABLE IF NOT EXISTS exchange_rate_history (
                                                     history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     currency_code VARCHAR(3) NOT NULL,
                                                     old_rate DECIMAL(10,4),
                                                     new_rate DECIMAL(10,4) NOT NULL,
                                                     changed_by VARCHAR(50),
                                                     change_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                     change_reason VARCHAR(200),

                                                     FOREIGN KEY (currency_code) REFERENCES exchange_rates(currency_code)
                                                         ON DELETE CASCADE ON UPDATE CASCADE,

                                                     INDEX idx_history_currency (currency_code),
                                                     INDEX idx_history_timestamp (change_timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru audit log (pentru securitate și tracing)
CREATE TABLE IF NOT EXISTS audit_log (
                                         log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id VARCHAR(50),
                                         user_type VARCHAR(20), -- 'CUSTOMER', 'MANAGER', 'SYSTEM'
                                         action_type VARCHAR(50) NOT NULL, -- 'LOGIN', 'LOGOUT', 'TRANSACTION', 'ACCOUNT_MODIFICATION'
                                         action_details TEXT,
                                         ip_address VARCHAR(45),
                                         user_agent VARCHAR(500),
                                         timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         status VARCHAR(20), -- 'SUCCESS', 'FAILURE'

    -- Indexuri pentru interogări de audit
                                         INDEX idx_audit_user (user_id, user_type),
                                         INDEX idx_audit_timestamp (timestamp),
                                         INDEX idx_audit_action (action_type),
                                         INDEX idx_audit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela pentru limite și reguli bancare
CREATE TABLE IF NOT EXISTS banking_rules (
                                             rule_id VARCHAR(50) PRIMARY KEY,
                                             rule_name VARCHAR(100) NOT NULL,
                                             rule_value DECIMAL(15,2) NOT NULL,
                                             description VARCHAR(200),
                                             last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             modified_by VARCHAR(50),

                                             INDEX idx_rules_name (rule_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TRIGGERE PENTRU VALIDARE ȘI AUDIT
-- =============================================

-- Trigger pentru actualizarea automată a last_updated în exchange_rates
DELIMITER $$
CREATE TRIGGER before_exchange_rate_update
    BEFORE UPDATE ON exchange_rates
    FOR EACH ROW
BEGIN
    SET NEW.last_updated = CURRENT_TIMESTAMP;

    -- Înregistrează în istoric
    INSERT INTO exchange_rate_history (currency_code, old_rate, new_rate, change_reason)
    VALUES (OLD.currency_code, OLD.rate_to_mdl, NEW.rate_to_mdl, 'Rate update');
END$$
DELIMITER ;

-- Trigger pentru audit la crearea conturilor
DELIMITER $$
CREATE TRIGGER after_account_insert
    AFTER INSERT ON accounts
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, user_type, action_type, action_details)
    VALUES (NEW.customer_id, 'CUSTOMER', 'ACCOUNT_CREATION',
            CONCAT('Cont creat: ', NEW.account_number, ', Tip: ', NEW.account_type));
END$$
DELIMITER ;

-- =============================================
-- VIZUALIZĂRI (VIEWS) PENTRU RAPOARTE
-- =============================================

-- View pentru soldurile totale ale clienților
CREATE OR REPLACE VIEW v_customer_balances AS
SELECT
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(a.account_number) as total_accounts,
    SUM(a.balance_mdl) as total_mdl,
    SUM(a.balance_eur) as total_eur,
    SUM(a.balance_usd) as total_usd,
    SUM(a.balance_gbp) as total_gbp,
    SUM(a.balance_ron) as total_ron,
    c.is_active
FROM customers c
         LEFT JOIN accounts a ON c.customer_id = a.customer_id
GROUP BY c.customer_id, c.first_name, c.last_name, c.email, c.is_active;

-- View pentru activitatea zilnică
CREATE OR REPLACE VIEW v_daily_activity AS
SELECT
    DATE(t.timestamp) as activity_date,
    COUNT(*) as total_transactions,
    SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_transactions,
    SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END) as failed_transactions,
    SUM(t.amount) as total_amount,
    t.currency
FROM transactions t
GROUP BY DATE(t.timestamp), t.currency;

-- =============================================
-- RÂNDURI INIȚIALE (DEFAULT DATA)
-- =============================================

-- Ratele de schimb inițiale
INSERT INTO exchange_rates (currency_code, rate_to_mdl, description) VALUES
                                                                         ('EUR', 19.4500, 'Euro'),
                                                                         ('USD', 17.5500, 'Dolar american'),
                                                                         ('GBP', 22.1000, 'Liră sterlină'),
                                                                         ('RON', 4.0000, 'Leu românesc')
ON DUPLICATE KEY UPDATE rate_to_mdl = VALUES(rate_to_mdl), last_updated = CURRENT_TIMESTAMP;

-- Reguli bancare default
INSERT INTO banking_rules (rule_id, rule_name, rule_value, description) VALUES
                                                                            ('MIN_DEPOSIT', 'Depunere minimă', 1.00, 'Suma minimă pentru depunere (MDL)'),
                                                                            ('MIN_BALANCE', 'Sold minim', 10.00, 'Sold minim pentru menținerea contului (MDL)'),
                                                                            ('MAX_LOGIN_ATTEMPTS', 'Încercări login maxime', 3.00, 'Numărul maxim de încercări de login eșuate'),
                                                                            ('ACCOUNT_LOCK_TIME', 'Timp blocare cont', 30.00, 'Timpul de blocare al contului în minute'),
                                                                            ('DAILY_WITHDRAWAL_LIMIT', 'Limită retragere zilnică', 5000.00, 'Limită implicită de retragere zilnică (MDL)'),
                                                                            ('MIN_WITHDRAWAL_LIMIT', 'Limită retragere minimă', 100.00, 'Limită minimă ce poate fi setată (MDL)'),
                                                                            ('EXCHANGE_COMMISSION', 'Comision schimb valutar', 0.50, 'Comision pentru schimb valutar (%)')
ON DUPLICATE KEY UPDATE rule_value = VALUES(rule_value), last_modified = CURRENT_TIMESTAMP;

-- Manager admin default (parola: Admin1234 - trebuie hash-uită în producție)
INSERT INTO bank_managers (employee_id, username, password_hash, first_name, last_name, email, access_level) VALUES
    ('MGR001', 'admin', '$2a$10$YourHashedPasswordHere', 'Admin', 'System', 'admin@bancacomerciala.md', 'SUPER_ADMIN')
ON DUPLICATE KEY UPDATE
                     password_hash = VALUES(password_hash),
                     access_level = VALUES(access_level);
