update audit_events
set metadata = replace(metadata, '"fromNomisId"', '"from_nomis_id"')
where event_type = 'JOURNEY_PRICE'
  and metadata like '%"fromNomisId"%';

update audit_events
set metadata = replace(metadata, '"toNomisId"', '"to_nomis_id"')
where event_type = 'JOURNEY_PRICE'
  and metadata like '%"toNomisId"%';

update audit_events
set metadata = replace(metadata, '"effectiveYear"', '"effective_year"')
where event_type = 'JOURNEY_PRICE'
  and metadata like '%"effectiveYear"%';

update audit_events
   set metadata = replace(metadata, '"price"', '"new_price"')
 where event_type = 'JOURNEY_PRICE'
   and metadata like '%"price"%';
