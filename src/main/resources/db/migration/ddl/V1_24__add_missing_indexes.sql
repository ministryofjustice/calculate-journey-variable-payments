create index if not exists from_nomis_agency_id_idx on journeys(from_nomis_agency_id);
create index if not exists to_nomis_agency_id_idx on journeys(to_nomis_agency_id);
create index if not exists effective_year_idx on journeys(effective_year);
create index if not exists effective_year_idx on prices(effective_year);
create index if not exists price_id_idx on price_exceptions(price_id);
create index if not exists month_idx on price_exceptions(month);