update audit_events
   set metadata_key = jsonb_extract_path_text(metadata::jsonb, 'supplier') || '-' || jsonb_extract_path_text(metadata::jsonb, 'from_nomis_id') || '-' || jsonb_extract_path_text(metadata::jsonb, 'to_nomis_id')
 where event_type = 'JOURNEY_PRICE'
   and metadata_key is null;

update audit_events
   set metadata_key = jsonb_extract_path_text(metadata::jsonb, 'nomis_id')
 where event_type = 'LOCATION'
   and metadata_key is null;
