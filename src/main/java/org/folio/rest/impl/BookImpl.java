package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.folio.rest.jaxrs.model.NataliaBookCollection;
import org.folio.rest.jaxrs.resource.NataliaBook;
import org.folio.repository.BookRepository;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import static io.vertx.core.Future.succeededFuture;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;

public class BookImpl implements NataliaBook {

  @Autowired
  private BookRepository repository;


  public BookImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void getNataliaBook(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    repository.findAll(tenantId, vertxContext.owner())
      .thenCompose(list -> {
        asyncResultHandler.handle(succeededFuture(
          GetNataliaBookResponse.respond200WithApplicationJson(
            new NataliaBookCollection()
              .withBooks(list)
              .withTotalRecords(list.size()))));
        return null;
    }).exceptionally(e -> {
      asyncResultHandler.handle(succeededFuture(GetNataliaBookResponse.status(INTERNAL_SERVER_ERROR).build()));
      return null;
    });
  }

  @Override
  public void postNataliaBook(String contentType, org.folio.rest.jaxrs.model.NataliaBook entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    repository.save(entity, tenantId, vertxContext.owner())
      .thenCompose(aVoid -> {
        asyncResultHandler.handle(succeededFuture(PostNataliaBookResponse.respond200WithApplicationJson(entity)));
        return null;
      }).exceptionally(e -> {
          asyncResultHandler.handle(succeededFuture(PostNataliaBookResponse.status(INTERNAL_SERVER_ERROR).build()));
          return null;
      });
  }

  @Override
  public void getNataliaBookById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    repository.findById(id, tenantId, vertxContext.owner())
      .thenCompose(book -> {
        asyncResultHandler.handle(succeededFuture(
          GetNataliaBookByIdResponse.respond200WithApplicationJson(book)));
        return null;
    }).exceptionally(e -> {
      asyncResultHandler.handle(succeededFuture(GetNataliaBookResponse.status(INTERNAL_SERVER_ERROR).build()));
      return null;
    });
  }

  @Override
  public void deleteNataliaBookById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(succeededFuture(DeleteNataliaBookByIdResponse.status(NOT_IMPLEMENTED).build()));
  }

  @Override
  public void putNataliaBookById(String id, String lang, org.folio.rest.jaxrs.model.NataliaBook entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(succeededFuture(PutNataliaBookByIdResponse.status(NOT_IMPLEMENTED).build()));
  }
}
