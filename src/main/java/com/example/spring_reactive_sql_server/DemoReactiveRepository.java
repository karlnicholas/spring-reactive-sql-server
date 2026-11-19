package com.example.spring_reactive_sql_server;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.r2dbc.spi.Readable;

/**
 * Reactive access to {@code dbo.demoreactive} through Spring's {@link DatabaseClient}.
 *
 * <p>{@code spring-boot-starter-r2dbc} auto-configures that client from
 * {@code spring.r2dbc.url}. This class does not use Spring Data repositories.
 *
 * <p>Inserts and updates read the identity with {@code OUTPUT INSERTED}.
 * {@code Statement.returnGeneratedValues} throws on {@code r2dbc-mssql} 1.0.1.
 */
@Repository
public class DemoReactiveRepository {

	private final DatabaseClient databaseClient;

	public DemoReactiveRepository(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	public Flux<DemoReactiveRow> findAll() {
		return databaseClient.sql("SELECT id, note FROM dbo.demoreactive ORDER BY id")
			.map(this::toRow)
			.all();
	}

	public Mono<DemoReactiveRow> findById(int id) {
		return databaseClient.sql("SELECT id, note FROM dbo.demoreactive WHERE id = :id")
			.bind("id", id)
			.map(this::toRow)
			.one();
	}

	public Mono<DemoReactiveRow> insert(String note) {
		return databaseClient.sql("""
				INSERT INTO dbo.demoreactive (note)
				OUTPUT INSERTED.id, INSERTED.note
				VALUES (:note)
				""")
			.bind("note", note)
			.map(this::toRow)
			.one();
	}

	/**
	 * Empty when no row has this id. {@code OUTPUT} returns the updated row,
	 * so a miss is an empty result rather than a row count of zero.
	 */
	public Mono<DemoReactiveRow> update(int id, String note) {
		return databaseClient.sql("""
				UPDATE dbo.demoreactive
				SET note = :note
				OUTPUT INSERTED.id, INSERTED.note
				WHERE id = :id
				""")
			.bind("id", id)
			.bind("note", note)
			.map(this::toRow)
			.one();
	}

	public Mono<Boolean> deleteById(int id) {
		return databaseClient.sql("DELETE FROM dbo.demoreactive WHERE id = :id")
			.bind("id", id)
			.fetch()
			.rowsUpdated()
			.map(updated -> updated > 0);
	}

	private DemoReactiveRow toRow(Readable row) {
		return new DemoReactiveRow(row.get("id", Integer.class), row.get("note", String.class));
	}

}
