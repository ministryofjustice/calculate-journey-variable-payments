insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J1', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-01 11:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON1', 'M2', null, to_timestamp('2020-12-01 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON2', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J1');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J3', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-01 12:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON3', 'M4', null, to_timestamp('2020-12-01 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON4', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J3');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J20', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'WYI', 'M20', null, to_timestamp('2020-12-02 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'cancelled', 'BOG', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J20');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J21', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-02 14:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'WYI', 'M20', null, to_timestamp('2020-12-02 13:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'GNI', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J21');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J30', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-03 17:00:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'WYI', 'M30', null, to_timestamp('2020-12-03 12:00:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'GNI', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J30');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J31', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-04 11:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'GNI', 'M30', null, to_timestamp('2020-12-04 09:00:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'BOG', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '456') as tmp where not exists (select journey_id from journeys where journey_id = 'J31');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J40', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-04 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'COURT1', 'M40', null, to_timestamp('2020-12-04 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'POLICE1', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J40');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J41', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-05 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'POLICE1', 'M40', null, to_timestamp('2020-12-05 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON1', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '456') as tmp where not exists (select journey_id from journeys where journey_id = 'J41');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J50', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-05 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON1', 'M50', null, to_timestamp('2020-12-05 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON2', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J50');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J51', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON2', 'M50', null, to_timestamp('2020-12-06 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'cancelled', 'GEOAMEY', 'PRISON3', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J51');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J52', true, to_timestamp('2020-09-08 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-12-06 13:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON2', 'M50', null, to_timestamp('2020-12-06 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON4', to_timestamp('2020-06-16 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '456') as tmp where not exists (select journey_id from journeys where journey_id = 'J52');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'eda7acea-803e-4640-9cda-3374f3af40ea', true, to_timestamp('2021-01-18 12:14:50.941881', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'WYI', 'M60', 'FAKE JOURNEY ADDED FOR CANCELLED BILLABLE MOVE', to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 'cancelled', 'GEOAMEY', 'GNI', to_timestamp('2021-01-18 12:14:50.941870', 'YYYY-MM-DD HH24-MI-SS.US'), null) as tmp where not exists (select journey_id from journeys where journey_id = 'eda7acea-803e-4640-9cda-3374f3af40ea');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select '10e4d2a7-f872-4eea-9b9b-ae230627edf0', true, to_timestamp('2021-01-18 12:14:50.942044', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'WYI', 'M61', 'FAKE JOURNEY ADDED FOR CANCELLED BILLABLE MOVE', to_timestamp(null, 'YYYY-MM-DD HH24-MI-SS.US'), 'cancelled', 'GEOAMEY', 'GNI', to_timestamp('2021-01-18 12:14:50.942042', 'YYYY-MM-DD HH24-MI-SS.US'), null) as tmp where not exists (select journey_id from journeys where journey_id = '10e4d2a7-f872-4eea-9b9b-ae230627edf0');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J20200820-1', true, to_timestamp('2020-08-20 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2020-08-20 11:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2020, 'PRISON1', 'M20200820-1', null, to_timestamp('2020-08-20 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON2', to_timestamp('2020-08-20 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J20200820-1');
insert into journeys (journey_id, billable, client_timestamp, drop_off, effective_year, from_nomis_agency_id, move_id, notes, pick_up, state, supplier, to_nomis_agency_id, updated_at, vehicle_registration) select * from (select 'J20210904-1', true, to_timestamp('2020-09-04 12:49:00.000000', 'YYYY-MM-DD HH24-MI-SS.US'), to_timestamp('2021-09-04 11:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 2021, 'PRISON1', 'M20210904-1', null, to_timestamp('2021-09-04 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), 'completed', 'GEOAMEY', 'PRISON2', to_timestamp('2021-09-04 10:20:30.000000', 'YYYY-MM-DD HH24-MI-SS.US'), '123') as tmp where not exists (select journey_id from journeys where journey_id = 'J20210904-1');
