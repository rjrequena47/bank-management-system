-- Flyway migration: Create transfers table
-- Version: V3
-- Description: create_transfers_table

CREATE TABLE transfers (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id         UUID NOT NULL REFERENCES accounts(id),
    destination_account_id    UUID REFERENCES accounts(id),
    destination_account_number VARCHAR(34) NOT NULL,
    amount                    DECIMAL(19, 2) NOT NULL,
    concept                   VARCHAR(255),
    status                    VARCHAR(20) NOT NULL,
    reference_number          VARCHAR(50) NOT NULL UNIQUE,
    debit_transaction_id      UUID REFERENCES transactions(id),
    credit_transaction_id     UUID REFERENCES transactions(id),
    created_at                TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transfers_source_account_id ON transfers (source_account_id);
CREATE INDEX idx_transfers_reference_number  ON transfers (reference_number);
