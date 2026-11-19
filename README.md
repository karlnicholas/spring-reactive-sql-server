# spring-reactive-sql-server

A small Spring Boot example of a reactive REST API on Microsoft SQL Server.

The HTTP stack is WebFlux. Queries go through `DatabaseClient` from `spring-boot-starter-r2dbc` and the driver [`org.mssqllib.r2dbc.mssql:r2dbc-mssql:1.0.1`](https://mssqllib.org/). There is one table, `dbo.demoreactive`, with an identity `id` and a `VARCHAR(255)` column named `note`.

Flyway creates that table at startup. See [Why Flyway](#why-flyway).

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

The script is safe to run again. It creates the database and the SQL login if they are missing. `CHECK_POLICY` is off because this published password does not meet Windows complexity rules. The login is `db_owner` of `demoreactive` only, so the same account can migrate and serve the API. A production service would use a separate migration login.

## Run

```text
mvnw spring-boot:run
```

On Windows:

```text
mvnw.cmd spring-boot:run
```

Flyway applies `src/main/resources/db/migration/V1__create_demoreactive.sql` before the server accepts traffic. The API listens on port 8080.

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

## Why Flyway

Schema changes belong in versioned scripts, not in repository code and not in `schema.sql` that re-runs by hand. Flyway is the usual Spring Boot tool for that. It records what already ran in `dbo.flyway_schema_history`.

Flyway speaks JDBC, not R2DBC. That is a property of Flyway, and it is also the arrangement Spring Boot expects: migrations are a short blocking step during startup, then the application uses the reactive pool. This project keeps the two connections separate:

- `spring.r2dbc.url` is the reactive application
- `spring.flyway.url`, `spring.flyway.user`, and `spring.flyway.password` are the JDBC migration

There is no `spring.datasource.*`. Setting `spring.flyway.url` tells Boot to build a DataSource for Flyway alone, so request handling does not gain a JDBC pool.

`spring.sql.init` is the lower-level alternative (a `schema.sql` on the R2DBC connection). Use that only for a throwaway script. It does not track versions. Do not run both.

## Driver notes

Do not also depend on `io.r2dbc:r2dbc-mssql`. Both jars register an `io.r2dbc.spi.ConnectionFactoryProvider`, and which one answers is then undefined. The URL scheme for this driver is `r2dbc:mssql://`, and the port is required.

`Statement.returnGeneratedValues` throws. Inserts and updates in `DemoReactiveRepository` use `OUTPUT INSERTED` to read the identity.

`note` is `VARCHAR(255)`, not `varchar(max)`. A max column is a large object. With the driver's default `materializeLobs=false`, `row.get("note", String.class)` cannot read that stream.

`reactor-core` is excluded from the driver dependency so Spring Boot's Reactor version is the one on the classpath. `io.r2dbc:r2dbc-pool` is declared directly because `spring-boot-starter-r2dbc` does not bring it, and `spring.r2dbc.pool.*` applies only when that pool is present.

The Initializr shell also had `spring-boot-starter-webmvc`. That starter selects the servlet stack when both stacks are on the classpath, so it is not in this pom.
