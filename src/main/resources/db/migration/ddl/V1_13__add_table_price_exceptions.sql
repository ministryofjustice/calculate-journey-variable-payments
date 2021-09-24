create table if not exists price_exceptions
(
    price_exception_id uuid not null constraint price_exceptions_pkey primary key,
    price_id           uuid not null,
    month              int not null,
    price_in_pence     int not null,
    constraint PRICE_ID_FK foreign key (price_id) references prices
);

create unique index if not exists price_id_month_index ON price_exceptions (price_id, month);
