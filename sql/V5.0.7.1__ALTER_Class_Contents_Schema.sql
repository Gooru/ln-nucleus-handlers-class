ALTER TABLE class_contents ADD COLUMN meeting_id text;
ALTER TABLE class_contents ADD COLUMN meeting_url text;
ALTER TABLE class_contents ADD COLUMN meeting_starttime timestamp without time zone;
ALTER TABLE class_contents ADD COLUMN meeting_endtime timestamp without time zone;
ALTER TABLE class_contents ADD COLUMN meeting_timezone text;
