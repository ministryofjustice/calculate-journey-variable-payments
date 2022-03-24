update moves set move_month = extract(month from move_date);
update moves set move_year = extract(year from move_date);