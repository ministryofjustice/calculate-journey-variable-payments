insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR1', 'PE1', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR1');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR2', 'PE2', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR2');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR3', 'PE3', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR3');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR4', 'PE4', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR4');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR5', 'PE5', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR5');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR6', 'PE6', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR6');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR7', 'PE7', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR7');
insert into profiles (profile_id, person_id, updated_at) select * from (select 'PR8', 'PE8', '2020-06-16 10:20:30.000000'::timestamp) as tmp where not exists (select profile_id from profiles where profile_id = 'PR8');
