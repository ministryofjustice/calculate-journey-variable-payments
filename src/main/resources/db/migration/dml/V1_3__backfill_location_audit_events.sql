CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

insert into audit_events
  select uuid_generate_v4(),
         l.added_at,
         'LOCATION',
         '{"nomis_id" : "'|| l.nomis_agency_id ||'", "new_name" : "'|| l.site_name ||'", "new_type" : "'|| l.location_type ||'"}',
         '_TERMINAL_'
    from locations l
   where not exists (select 1 from audit_events ae where ae.metadata like '%"'|| l.nomis_agency_id ||'"%' and ae.event_type = 'LOCATION');
