ALTER TABLE users ADD COLUMN role VARCHAR(30) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD COLUMN username VARCHAR(60) NULL;
CREATE UNIQUE INDEX uk_users_username ON users (username);

CREATE TABLE professional_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    prediction_id BIGINT NOT NULL,
    reviewer_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    consented_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    agrees_with_ai BOOLEAN NULL,
    comment VARCHAR(2000) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_professional_reviews_prediction UNIQUE (prediction_id),
    CONSTRAINT fk_professional_reviews_prediction FOREIGN KEY (prediction_id) REFERENCES predictions (id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
);
CREATE INDEX idx_professional_reviews_queue ON professional_reviews (status, consented_at);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    prediction_id BIGINT NULL,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(500) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_prediction FOREIGN KEY (prediction_id) REFERENCES predictions (id) ON DELETE SET NULL
);
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at);
