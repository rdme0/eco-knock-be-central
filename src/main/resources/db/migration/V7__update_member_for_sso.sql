ALTER TABLE member
    ADD COLUMN sso_member_id BIGINT;

UPDATE member
SET sso_member_id = id
WHERE sso_member_id IS NULL;

ALTER TABLE member
    ALTER COLUMN sso_member_id SET NOT NULL;

ALTER TABLE member
    ADD CONSTRAINT uk_member_sso_member_id UNIQUE (sso_member_id);

ALTER TABLE member
    DROP COLUMN provider;
