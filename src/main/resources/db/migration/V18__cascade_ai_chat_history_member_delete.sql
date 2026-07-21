ALTER TABLE ai_chat_history
    DROP CONSTRAINT fk_ai_chat_history_member;

ALTER TABLE ai_chat_history
    ADD CONSTRAINT fk_ai_chat_history_member
        FOREIGN KEY (member_id)
        REFERENCES member (id)
        ON DELETE CASCADE;
