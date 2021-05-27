alter table audit_events add column if not exists metadata_key varchar(255);

create index if not exists audit_events_et_mdk_idx on audit_events (event_type, metadata_key);