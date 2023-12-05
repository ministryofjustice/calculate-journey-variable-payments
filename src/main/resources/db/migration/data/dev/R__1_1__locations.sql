CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('709fbee3-7fe6-4584-a8dc-f12481165bfa'), current_timestamp, 'PR', 'PRISON1', 'PRISON ONE', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'PRISON1');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('13c46837-c5c9-45a4-83d5-5a0d1438ff3c'), current_timestamp, 'PS', 'POLICE1', 'POLICE ONE', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'POLICE1');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('612ec4d3-9cfa-4c89-ad39-3f02acc8b41d'), current_timestamp, 'PR', 'PRISON2', 'PRISON TWO', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'PRISON2');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid_generate_v4(), current_timestamp, 'PR', 'PRISON3', 'PRISON THREE', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'PRISON3');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid_generate_v4(), current_timestamp, 'PR', 'PRISON4', 'PRISON FOUR', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'PRISON4');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid_generate_v4(), current_timestamp, 'CC', 'COURT1', 'COURT ONE', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'COURT1');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('709fbee3-7fe6-4584-a8dc-f12481165bfa'), current_timestamp, 'PR', 'PRISON1L', 'PRISON ONE L', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'PRISON1L');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('13chf837-c5c9-45a4-83d5-5a0d1438ff3c'), current_timestamp, 'PS', 'POLICE1L', 'POLICE ONE L', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'POLICE1L');
insert into locations (location_id, added_at, location_type, nomis_agency_id, site_name, updated_at) select uuid('13c46as7-c5c9-45a4-83d5-5a0d1438ff3c'), current_timestamp, 'PS', 'POLICE2L', 'POLICE TWO L', current_timestamp where not exists (select 1 from locations where nomis_agency_id = 'POLICE2L');
