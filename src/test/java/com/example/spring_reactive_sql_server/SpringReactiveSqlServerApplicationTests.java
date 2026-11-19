package com.example.spring_reactive_sql_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Hits the real local SQL Server from {@code application.properties}.
 * Flyway creates the table before the first request.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SpringReactiveSqlServerApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	void createsReadsUpdatesAndDeletesARow() {
		DemoReactiveRow created = client.post()
			.uri("/demoreactive")
			.bodyValue(new DemoReactiveRow(null, "hello"))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueMatches("Location", ".*/demoreactive/\\d+")
			.expectBody(DemoReactiveRow.class)
			.returnResult()
			.getResponseBody();

		client.get()
			.uri("/demoreactive/{id}", created.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(created.id())
			.jsonPath("$.note").isEqualTo("hello");

		client.put()
			.uri("/demoreactive/{id}", created.id())
			.bodyValue(new DemoReactiveRow(null, "updated"))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.note").isEqualTo("updated");

		client.delete()
			.uri("/demoreactive/{id}", created.id())
			.exchange()
			.expectStatus().isNoContent();

		client.get()
			.uri("/demoreactive/{id}", created.id())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void rejectsAMissingNote() {
		client.post()
			.uri("/demoreactive")
			.bodyValue(new DemoReactiveRow(null, null))
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void missingRowIsNotFound() {
		client.get().uri("/demoreactive/{id}", -1).exchange().expectStatus().isNotFound();
		client.delete().uri("/demoreactive/{id}", -1).exchange().expectStatus().isNotFound();
	}

}
