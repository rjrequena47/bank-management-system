-- Flyway migration: Create accounts table
-- Version: V1
-- Description: create_accounts_table

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(255) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    alias VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    daily_withdrawal_limit NUMERIC(19, 2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);
