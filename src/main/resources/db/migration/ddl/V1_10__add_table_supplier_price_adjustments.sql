create table if not exists price_adjustments
(
    id              uuid not null constraint price_adjustments_pkey primary key,
    added_at        timestamp not null,
    supplier        varchar(255) not null,
    constraint SUPPLIER_UNIQUE unique (supplier)
);
