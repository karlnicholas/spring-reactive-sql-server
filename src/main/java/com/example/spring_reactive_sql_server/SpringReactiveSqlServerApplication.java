package com.example.spring_reactive_sql_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Reactive REST demo against one SQL Server table.
 *
 * <p>Flyway migrates {@code dbo.demoreactive} through JDBC during startup.
 * After that, {@link DemoReactiveController} serves HTTP on WebFlux and talks
 * to SQL Server through the R2DBC driver {@code org.mssqllib.r2dbc.mssql}.
 */
@SpringBootApplication
public class SpringReactiveSqlServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringReactiveSqlServerApplication.class, args);
	}

}
