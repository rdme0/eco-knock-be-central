alter table control_setting
    add column clean_air_quality_ratio_threshold INTEGER not null default 80;

alter table control_setting
    alter column clean_air_quality_ratio_threshold drop default;
