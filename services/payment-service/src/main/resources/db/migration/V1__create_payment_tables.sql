-- Create Payment Requests Table
CREATE TABLE payment_requests (
    id UUID PRIMARY KEY,
    requestor_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50),
    transaction_id UUID,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create Scheduled Payments Table
CREATE TABLE scheduled_payments (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    source_wallet_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    last_execution_date DATE,
    total_executions INT NOT NULL,
    completed_executions INT NOT NULL,
    max_executions INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create Scheduled Payment Executions Table
CREATE TABLE scheduled_payment_executions (
    id UUID PRIMARY KEY,
    scheduled_payment_id UUID NOT NULL REFERENCES scheduled_payments(id),
    transaction_id UUID,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(500),
    execution_date TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
);

-- Create Split Payments Table
CREATE TABLE split_payments (
    id UUID PRIMARY KEY,
    organizer_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    total_amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create Split Payment Participants Table
CREATE TABLE split_payment_participants (
    id UUID PRIMARY KEY,
    split_payment_id UUID NOT NULL REFERENCES split_payments(id),
    user_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    transaction_id UUID,
    paid BOOLEAN NOT NULL,
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create Indexes
CREATE INDEX idx_payment_requests_requestor_id ON payment_requests(requestor_id);
CREATE INDEX idx_payment_requests_recipient_id ON payment_requests(recipient_id);
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_payment_requests_reference_number ON payment_requests(reference_number);
CREATE INDEX idx_payment_requests_expiry_date ON payment_requests(expiry_date);

CREATE INDEX idx_scheduled_payments_sender_id ON scheduled_payments(sender_id);
CREATE INDEX idx_scheduled_payments_recipient_id ON scheduled_payments(recipient_id);
CREATE INDEX idx_scheduled_payments_status ON scheduled_payments(status);
CREATE INDEX idx_scheduled_payments_next_execution_date ON scheduled_payments(next_execution_date);

CREATE INDEX idx_split_payments_organizer_id ON split_payments(organizer_id);
CREATE INDEX idx_split_payments_status ON split_payments(status);
CREATE INDEX idx_split_payments_expiry_date ON split_payments(expiry_date);

CREATE INDEX idx_split_payment_participants_split_payment_id ON split_payment_participants(split_payment_id);
CREATE INDEX idx_split_payment_participants_user_id ON split_payment_participants(user_id);
CREATE INDEX idx_split_payment_participants_paid ON split_payment_participants(paid);