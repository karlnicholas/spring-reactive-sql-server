package com.example.spring_reactive_sql_server;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive CRUD for {@code dbo.demoreactive}. Each method returns a Reactor
 * publisher. WebFlux subscribes on the HTTP event loop, and the R2DBC driver
 * does the SQL without blocking that thread.
 */
@RestController
@RequestMapping("/demoreactive")
public class DemoReactiveController {

	private static final int NOTE_MAX_LENGTH = 255;

	private final DemoReactiveRepository repository;

	public DemoReactiveController(DemoReactiveRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public Flux<DemoReactiveRow> list() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public Mono<DemoReactiveRow> get(@PathVariable int id) {
		return repository.findById(id).switchIfEmpty(notFound(id));
	}

	@PostMapping
	public Mono<ResponseEntity<DemoReactiveRow>> create(@RequestBody DemoReactiveRow request) {
		String note = requiredNote(request);
		return repository.insert(note)
			.map(saved -> ResponseEntity.created(URI.create("/demoreactive/" + saved.id())).body(saved));
	}

	@PutMapping("/{id}")
	public Mono<DemoReactiveRow> update(@PathVariable int id, @RequestBody DemoReactiveRow request) {
		String note = requiredNote(request);
		return repository.update(id, note).switchIfEmpty(notFound(id));
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable int id) {
		return repository.deleteById(id).flatMap(deleted -> {
			if (deleted) {
				return Mono.just(ResponseEntity.noContent().build());
			}
			return notFound(id);
		});
	}

	private static String requiredNote(DemoReactiveRow request) {
		if (request == null || request.note() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "note is required");
		}
		if (request.note().length() > NOTE_MAX_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "note must be at most 255 characters");
		}
		return request.note();
	}

	private static <T> Mono<T> notFound(int id) {
		return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "no demoreactive row " + id));
	}

}
