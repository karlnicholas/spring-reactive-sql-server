-- One demo table. id is a SQL Server identity. note is a normal bounded
-- varchar, not varchar(max): max is a large object, and this R2DBC driver
-- streams those unless the connection sets materializeLobs=true.
CREATE TABLE dbo.demoreactive (
    id INT IDENTITY(1, 1) NOT NULL CONSTRAINT pk_demoreactive PRIMARY KEY,
    note VARCHAR(255) NOT NULL
);
