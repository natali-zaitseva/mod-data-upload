package org.folio.repository;

import io.vertx.core.Vertx;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.folio.rest.jaxrs.model.NataliaBook;

public interface BookRepository {

  CompletableFuture<List<NataliaBook>> findAll(String tenantId, Vertx vertx);

  CompletableFuture<NataliaBook> findById(String id, String tenantId, Vertx vertx);

  CompletableFuture<Void> save(NataliaBook book, String tenantId, Vertx vertx);
}
