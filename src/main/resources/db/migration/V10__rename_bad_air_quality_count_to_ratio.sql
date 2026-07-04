alter table control_setting
    rename column dark_duration_minutes to dark_detection_time_threshold;

alter table control_setting
    rename column bright_duration_minutes to bright_detection_time_threshold;

alter table control_setting
    rename column bad_air_quality_window_minutes to air_quality_detection_time_threshold;

alter table control_setting
    rename column bad_air_quality_min_bucket_count to bad_air_quality_ratio_threshold;

update control_setting
set bad_air_quality_ratio_threshold = 80
where id = 1;
