update audit_events
set metadata = replace(replace(metadata, '"multiplier" : ', '"multiplier" : "'), ', "details"', '", "details"')
where event_type = 'JOURNEY_PRICE_BULK_ADJUSTMENT'
  and metadata not like '%"multiplier" : "%';
