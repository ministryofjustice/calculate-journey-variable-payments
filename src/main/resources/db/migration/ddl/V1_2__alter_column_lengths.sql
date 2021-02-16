ALTER TABLE moves ALTER COLUMN cancellation_reason_comment TYPE varchar(1024);

ALTER TABLE moves ALTER COLUMN notes TYPE varchar(1024);

ALTER TABLE events ALTER COLUMN notes TYPE varchar(1024);

ALTER TABLE journeys ALTER COLUMN notes TYPE varchar(1024);

