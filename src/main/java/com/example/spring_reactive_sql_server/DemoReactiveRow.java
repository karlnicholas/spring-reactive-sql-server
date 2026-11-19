package com.example.spring_reactive_sql_server;

/**
 * One row of {@code dbo.demoreactive}.
 *
 * <p>{@code id} is assigned by SQL Server. Leave it null on a create or update
 * request. {@code note} is {@code VARCHAR(255)}.
 */
public record DemoReactiveRow(Integer id, String note) {
}
