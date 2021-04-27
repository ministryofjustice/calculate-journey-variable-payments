update audit_events
   set metadata = replace(replace(replace(metadata, '"name"', '"new_name"'), '"type"', '"new_type"'), '"nomisId"', '"nomis_id"')
 where event_type = 'LOCATION'
   and metadata like '%"name"%'
   and metadata like '%"type"%';
