alter table moves add column if not exists move_month integer;
alter table moves add column if not exists move_year integer;

create index if not exists moves_month_year_idx on moves(move_month, move_year);
