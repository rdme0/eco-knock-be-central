create table control_setting
(
    id                               BIGINT                   NOT NULL,
    created_at                       TIMESTAMP WITH TIME ZONE,
    updated_at                       TIMESTAMP WITH TIME ZONE,
    enabled                          BOOLEAN                  NOT NULL,
    dark_lux_threshold               DOUBLE PRECISION         NOT NULL,
    bright_lux_threshold             DOUBLE PRECISION         NOT NULL,
    dark_duration_minutes            INTEGER                  NOT NULL,
    bright_duration_minutes          INTEGER                  NOT NULL,
    bad_air_quality_window_minutes   INTEGER                  NOT NULL,
    bad_air_quality_min_bucket_count INTEGER                  NOT NULL,
    cooldown_minutes                 INTEGER                  NOT NULL,
    CONSTRAINT pk_control_setting PRIMARY KEY (id)
);
