# spring-reactive-sql-server

A Spring Boot demonstration of a reactive REST API on Microsoft SQL Server, and a working example of the [mssqllib](http://www.mssqllib.org) R2DBC driver.

[mssqllib](http://www.mssqllib.org) is a public-domain R2DBC driver for SQL Server. It speaks TDS over JDK NIO sockets and Reactor. This project depends on [`org.mssqllib.r2dbc.mssql:r2dbc-mssql:1.0.1`](http://www.mssqllib.org). Installation, the current release, authentication, and the Spring Boot guide are on that site.

## What the demo does

The HTTP stack is WebFlux. Queries go through `DatabaseClient` from `spring-boot-starter-r2dbc` and the mssqllib driver. The API is create, read, update, and delete on one table, `dbo.demoreactive`, with an identity `id` and a `VARCHAR(255)` column named `note`.

`src/main/resources/db/migration/V1__create_demoreactive.sql` creates that table when the application starts. The API listens on port 8080.

## Prerequisites

- Java 21
- SQL Server listening on `localhost:1433`
- A sysadmin connection for the one-time setup below (Windows authentication is typical)

The demo login is local only:

| | |
| --- | --- |
| Database | `demoreactive` |
| Login | `demoreactiveuser` |
| Password | `demoreactivepassword` |

## Create the database

From the project directory, as a sysadmin:

```text
sqlcmd -S localhost -E -b -i sql/create-database.sql
```

The script is safe to run again. It creates the database and the SQL login if they are missing. `CHECK_POLICY` is off because this published password does not meet Windows complexity rules. The login is `db_owner` of `demoreactive` only.

## Run

```text
mvnw spring-boot:run
```

On Windows:

```text
mvnw.cmd spring-boot:run
```

Try it. On Windows PowerShell, use `curl.exe` so PowerShell does not treat `curl` as `Invoke-WebRequest`:

```text
curl -i -X POST http://localhost:8080/demoreactive -H "Content-Type: application/json" -d "{\"note\":\"hello\"}"

curl http://localhost:8080/demoreactive

curl http://localhost:8080/demoreactive/1

curl -X PUT http://localhost:8080/demoreactive/1 -H "Content-Type: application/json" -d "{\"note\":\"updated\"}"

curl -i -X DELETE http://localhost:8080/demoreactive/1
```

`note` is required and may be at most 255 characters. An unknown id returns 404.

## Tests

`mvnw test` starts the application against the same local SQL Server and walks create, read, update, and delete. The database from the previous section has to exist first.

## The driver in this project

The connection URL uses the mssqllib scheme `r2dbc:mssql://`. Host, port, and database are required:

```text
spring.r2dbc.url=r2dbc:mssql://demoreactiveuser:demoreactivepassword@localhost:1433/demoreactive?trustServerCertificate=true
```

`trustServerCertificate=true` skips the local certificate check.

Keep `io.r2dbc:r2dbc-mssql` off this classpath. Both jars register an `io.r2dbc.spi.ConnectionFactoryProvider`, and which one answers is then undefined.

`Statement.returnGeneratedValues` throws on this driver. Inserts and updates in `DemoReactiveRepository` use `OUTPUT INSERTED` to read the identity.

`note` is `VARCHAR(255)`. With the driver's default `materializeLobs=false`, `row.get("note", String.class)` reads that bounded string.

`reactor-core` is excluded from the driver dependency so Spring Boot's Reactor version is the one on the classpath. The pom also declares `io.r2dbc:r2dbc-pool`, which `spring.r2dbc.pool.*` requires, and `spring-boot-starter-webflux`, so Boot starts the reactive server.

More Spring Boot wiring, including Microsoft Entra login, is in the [mssqllib Spring Boot guide](http://www.mssqllib.org/spring.html).
