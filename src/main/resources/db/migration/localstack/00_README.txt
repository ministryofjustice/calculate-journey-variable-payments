There is a rationale behind the naming and sequence of the files in this
directory. All files whose name begins with 'R__' are Flyway repeatable
migration files.

Refer to https://flywaydb.org/documentation/migration/sql for naming rules for
Flyway repeatable migration files.

In this directory, the description component of each file name begins with two
numbers separated by an underscore. The numbers are then followed by two
consecutive underscores and the name of the database table for which will be
populated by the execution of the SQL statements within the file.

The first number represents a level. Level 1 tables have no dependencies (e.g.
foreign key references) on any other table in the database. Level 2 tables
have a dependency on at least one level 1 table. Level 3 tables have a
dependency on at least one level 2 table and possibly other level 1 or 2
tables. Level 4 tables have a dependency on at least one level 3 table and
possibly other level 1, 2 or 3 tables. And so on...

At the time of writing there are 7 levels.

The second number is simply a sequence number within the level and ensures
that the numbers alone define the sequence in which the repeatable migration
files are executed by Flyway.
