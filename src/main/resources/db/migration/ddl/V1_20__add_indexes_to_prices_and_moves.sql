create index if not exists prices_supplier_year on prices(supplier, effective_year);
create unique index if not exists moves_reference on moves(reference);

