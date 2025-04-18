-- Create Wallets Table
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    wallet_type VARCHAR(50) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(user_id, currency)
);

-- Create Transactions Table
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    external_id VARCHAR(100),
    source_wallet_id UUID,
    target_wallet_id UUID,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    reference_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create index on wallets
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_external_id ON wallets(external_id);
CREATE INDEX idx_wallets_status ON wallets(status);

-- Create index on transactions
CREATE INDEX idx_transactions_external_id ON transactions(external_id);
CREATE INDEX idx_transactions_source_wallet_id ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_target_wallet_id ON transactions(target_wallet_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);