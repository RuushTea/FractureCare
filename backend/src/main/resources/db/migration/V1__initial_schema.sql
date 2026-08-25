CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    address VARCHAR(255) NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE predictions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    image_reference VARCHAR(100) NOT NULL,
    original_file_name VARCHAR(100) NOT NULL,
    image_content_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    predicted_class VARCHAR(30) NULL,
    risk_category VARCHAR(30) NULL,
    confidence DECIMAL(5,4) NULL,
    model_version VARCHAR(80) NULL,
    simulated BOOLEAN NOT NULL DEFAULT FALSE,
    failure_message VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_predictions_image_reference UNIQUE (image_reference),
    CONSTRAINT fk_predictions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_predictions_user_created ON predictions (user_id, created_at);

CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    prediction_id BIGINT NOT NULL,
    file_reference VARCHAR(100) NOT NULL,
    generated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reports_prediction UNIQUE (prediction_id),
    CONSTRAINT uk_reports_file_reference UNIQUE (file_reference),
    CONSTRAINT fk_reports_prediction FOREIGN KEY (prediction_id) REFERENCES predictions (id) ON DELETE CASCADE
);
