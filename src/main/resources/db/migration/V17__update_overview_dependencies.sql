ALTER TABLE overview_shortcut
    ALTER COLUMN icon_url DROP NOT NULL;

ALTER TABLE default_overview_shortcut
    ALTER COLUMN icon_url DROP NOT NULL;

ALTER TABLE overview_shortcut
    DROP CONSTRAINT fk_overviewshortcut_member;

ALTER TABLE overview_shortcut
    ADD CONSTRAINT fk_overviewshortcut_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE;

ALTER TABLE overview_layout
    DROP CONSTRAINT fk_overview_layout_member;

ALTER TABLE overview_layout
    ADD CONSTRAINT fk_overview_layout_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE;
