-- Local SQL Server setup for this demo.
-- Run once as a sysadmin. Windows authentication is the usual way:
--
--   sqlcmd -S localhost -E -b -i sql/create-database.sql
--
-- Flyway and the application then log in as demoreactiveuser.
-- That login is db_owner of demoreactive only, because this demo uses one
-- account for both migrations and the reactive API. A production app would
-- split those. CHECK_POLICY is off because the published demo password does
-- not satisfy Windows password complexity.

IF DB_ID(N'demoreactive') IS NULL
BEGIN
    CREATE DATABASE demoreactive;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'demoreactiveuser')
BEGIN
    CREATE LOGIN demoreactiveuser
        WITH PASSWORD = N'demoreactivepassword',
             CHECK_POLICY = OFF,
             CHECK_EXPIRATION = OFF,
             DEFAULT_DATABASE = demoreactive;
END
ELSE
BEGIN
    ALTER LOGIN demoreactiveuser
        WITH PASSWORD = N'demoreactivepassword',
             CHECK_POLICY = OFF,
             CHECK_EXPIRATION = OFF,
             DEFAULT_DATABASE = demoreactive;
END
GO

USE demoreactive;
GO

-- Skip this when the login already owns the database (it is dbo).
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals AS dp
    INNER JOIN sys.server_principals AS sp ON dp.sid = sp.sid
    WHERE sp.name = N'demoreactiveuser'
)
BEGIN
    CREATE USER demoreactiveuser FOR LOGIN demoreactiveuser;
END
GO

IF USER_ID(N'demoreactiveuser') IS NOT NULL
   AND IS_ROLEMEMBER(N'db_owner', N'demoreactiveuser') <> 1
BEGIN
    ALTER ROLE db_owner ADD MEMBER demoreactiveuser;
END
GO
