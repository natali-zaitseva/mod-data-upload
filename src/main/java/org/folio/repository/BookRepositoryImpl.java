package org.folio.repository;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rest.jaxrs.model.NataliaBook;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.ObjectMapperTool;
import org.springframework.stereotype.Component;
import static io.vertx.core.json.Json.encode;
import static java.util.UUID.randomUUID;
import static org.folio.repository.BooksConstants.BOOKS_TABLE_NAME;
import static org.folio.repository.BooksConstants.GET_BOOKS;
import static org.folio.repository.BooksConstants.GET_BOOK_BY_ID;
import static org.folio.repository.BooksConstants.POST_BOOK;

@Component
public class BookRepositoryImpl implements BookRepository {

  @Override
  public CompletableFuture<List<NataliaBook>> findAll(String tenantId, Vertx vertx) {
    final String query = String.format(GET_BOOKS, getBooksTableName(tenantId));
    PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);
    Future<ResultSet> future = Future.future();
    postgresClient.select(query, future);
    return mapResult(future, this::mapBooks);
  }

  @Override
  public CompletableFuture<NataliaBook> findById(String id, String tenantId, Vertx vertx) {
    final String query = String.format(GET_BOOK_BY_ID, getBooksTableName(tenantId));
    JsonArray parameters = new JsonArray().add(id);
    PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);
    Future<ResultSet> future = Future.future();
    postgresClient.select(query, parameters, future);
    return mapResult(future, this::mapBook);
  }

  @Override
  public CompletableFuture<Void> save(NataliaBook book, String tenantId, Vertx vertx) {
    final String query = String.format(POST_BOOK, getBooksTableName(tenantId));
    JsonArray parameters = new JsonArray().add(randomUUID().toString()).add(encode(book));
    PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);
    Future<UpdateResult> future = Future.future();
    postgresClient.execute(query, parameters, future);
    return mapVertxFuture(future).thenApply(result -> null);
  }

  private String getBooksTableName(String tenantId) {
    return PostgresClient.convertToPsqlStandard(tenantId) + "." + BOOKS_TABLE_NAME;
  }

  private List<org.folio.rest.jaxrs.model.NataliaBook> mapBooks(ResultSet resultSet) {
    return mapItems(resultSet.getRows(), row -> mapColumn(row, "jsonb", org.folio.rest.jaxrs.model.NataliaBook.class).orElse(null));
  }
  private org.folio.rest.jaxrs.model.NataliaBook mapBook(ResultSet resultSet) {
    return mapColumn(resultSet.getRows().get(0), "jsonb", org.folio.rest.jaxrs.model.NataliaBook.class).orElse(null);
  }

  public static <T, R> List<R> mapItems(Collection<T> source, Function<? super T, ? extends R> mapper) {
    Objects.requireNonNull(source, "Collection is null");
    return source.stream().map(mapper).collect(Collectors.toList());
  }

  public static <T> Optional<T> mapColumn(JsonObject row, String columnName, Class<T> tClass){
    try {
      return Optional.of(ObjectMapperTool.getMapper().readValue(row.getString(columnName), tClass));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static <T,U> CompletableFuture<T> mapResult(Future<U> future, Function<U, T> mapper) {
    CompletableFuture<T> result = new CompletableFuture<>();

    future.map(mapper)
      .map(result::complete)
      .otherwise(result::completeExceptionally);

    return result;
  }

  public static <T> CompletableFuture<T> mapVertxFuture(Future<T> future) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();

    future
      .map(completableFuture::complete)
      .otherwise(completableFuture::completeExceptionally);

    return completableFuture;
  }
}
