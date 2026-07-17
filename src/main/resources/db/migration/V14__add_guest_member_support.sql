ALTER TABLE member
    ALTER COLUMN sso_member_id DROP NOT NULL,
    ALTER COLUMN cohort DROP NOT NULL,
    ALTER COLUMN status DROP NOT NULL,
    ADD COLUMN guest_expires_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_member_guest_expires_at
    ON member (guest_expires_at)
    WHERE role = 'GUEST';
