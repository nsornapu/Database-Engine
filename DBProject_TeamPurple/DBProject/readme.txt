To execute the prograam, please run the following command:
"java -jar filepath\executableDavisBase.jar"

after the application initialization is completed, use "help;" command to understand various commands

remember that ";" is used as delimiter, so all the commands should be terminated with semicolon.

Below are the various commands davisbase supports:

CREATE TABLE table_name (column_name1 INT PRIMARY KEY, column_name2 data_type2 [NOT NULL],... );
CREATE INDEX index_name <table_name> (column_name);
SELECT * FROM table_name;
SELECT * FROM table_name WHERE <column_name> = <value>;
UPDATE <table_name> SET column_name = value WHERE primary_key=value;
INSERT INTO <table_name> (column_list) VALUES (value1, value2, value3,..);
DELETE FROM <table_name> WHERE primary_key = key_value; 
DROP TABLE table_name;
DROP INDEX ON table_name;
SHOW TABLES;
VERSION;
HELP;
EXIT;