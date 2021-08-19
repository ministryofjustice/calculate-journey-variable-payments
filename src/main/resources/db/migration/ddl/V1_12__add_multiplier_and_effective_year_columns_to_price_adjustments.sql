alter table price_adjustments add column if not exists multiplier double precision;
alter table price_adjustments add column if not exists effective_year integer;

alter table price_adjustments alter column multiplier set not null;
alter table price_adjustments alter column effective_year set not null;
