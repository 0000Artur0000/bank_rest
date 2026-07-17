CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_username_not_blank CHECK (BTRIM(username) <> ''),
    CONSTRAINT chk_users_full_name_not_blank CHECK (BTRIM(full_name) <> ''),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_number TEXT NOT NULL,
    number_fingerprint VARCHAR(64) NOT NULL,
    last_four VARCHAR(4) NOT NULL,
    owner_id BIGINT NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    CONSTRAINT uk_cards_number_fingerprint UNIQUE (number_fingerprint),
    CONSTRAINT fk_cards_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_cards_fingerprint CHECK (number_fingerprint ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_cards_last_four CHECK (last_four ~ '^[0-9]{4}$'),
    CONSTRAINT chk_cards_status CHECK (status IN ('ACTIVE', 'BLOCKED')),
    CONSTRAINT chk_cards_balance CHECK (balance >= 0)
);

CREATE INDEX idx_cards_owner_id ON cards (owner_id);
