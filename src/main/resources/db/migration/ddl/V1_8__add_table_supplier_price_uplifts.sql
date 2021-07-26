create table if not exists supplier_price_uplifts
(
    id              uuid not null constraint supplier_price_uplifts_pkey primary key,
    added_at        timestamp not null,
    effective_year  integer not null,
    multiplier      double precision not null,
    supplier        varchar(255) not null,
    constraint SUPPLIER_UNIQUE unique (supplier)
);
