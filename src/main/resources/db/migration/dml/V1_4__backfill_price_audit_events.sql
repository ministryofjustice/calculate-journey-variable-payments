CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

insert into audit_events
select uuid_generate_v4() event_id,
       p.added_at created_at,
       'JOURNEY_PRICE' event_type,
       '{"supplier" : "' || p.supplier || '", "from_nomis_id" : "'|| lf.nomis_agency_id ||'", "to_nomis_id" : "'|| lt.nomis_agency_id ||'", "effective_year" : '|| p.effective_year ||', "new_price" : '|| cast(p.price_in_pence / 100.0 as decimal(15,2)) ||'}' metadata,
       '_TERMINAL_' username
  from prices p
       left join locations lf on p.from_location_id = lf.location_id
       left join locations lt on p.to_location_id = lt.location_id
 where not exists (select 1
                     from audit_events ae
                    where jsonb_extract_path_text(ae.metadata::jsonb, 'supplier') = p.supplier
                      and jsonb_extract_path_text(ae.metadata::jsonb, 'from_nomis_id') = lf.nomis_agency_id
                      and jsonb_extract_path_text(ae.metadata::jsonb, 'to_nomis_id') = lt.nomis_agency_id
                      and ae.event_type = 'JOURNEY_PRICE');
