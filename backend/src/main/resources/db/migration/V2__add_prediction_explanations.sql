ALTER TABLE predictions ADD COLUMN explanation_summary VARCHAR(1200) NULL;
ALTER TABLE predictions ADD COLUMN explanation_confidence_meaning VARCHAR(1200) NULL;
ALTER TABLE predictions ADD COLUMN explanation_next_step VARCHAR(1200) NULL;
ALTER TABLE predictions ADD COLUMN explanation_questions VARCHAR(1000) NULL;
ALTER TABLE predictions ADD COLUMN explanation_source VARCHAR(30) NULL;
ALTER TABLE predictions ADD COLUMN explanation_model VARCHAR(120) NULL;
