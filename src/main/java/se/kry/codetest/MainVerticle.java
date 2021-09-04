package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<Service, String> services = new HashMap<>();
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    insertKry();
    setRoutes(router);
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    getRoute(router);
    postRoute(router);
    deleteRoute(router);
    putRoute(router);
  }

  private void getRoute(Router router) {
    router.get("/service").handler(req -> {
      String sql = "SELECT url, creationDate, name FROM service";
      connector.query(sql).setHandler(done -> {
        if(done.succeeded()) {
          done.result().getResults().forEach(service -> {
            Service s = new Service(service.getString(0),
                                    service.getString(1),
                                    service.getString(2));
            boolean serviceFound = services.containsKey(s);
            if (!serviceFound) {
              services.put(s, "UNKNOWN");
            } else {
              String value = services.remove(s);
              services.put(s, value);
            }
          });
          List<JsonObject> jsonServices = services
                  .entrySet()
                  .stream()
                  .map(service ->
                          new JsonObject()
                                  .put("name", service.getKey().getName())
                                  .put("creationDate", service.getKey().getCreationDate())
                                  .put("url", service.getKey().getUrl())
                                  .put("status", service.getValue()))
                  .collect(Collectors.toList());
          System.out.println("Get Services.");
          req.response()
                  .putHeader("content-type", "application/json")
                  .end(new JsonArray(jsonServices).encode());
        } else {
          System.out.println("Error retrieving services");
          done.cause().printStackTrace();
        }
      });
    });
  }

  // Add a URL to the services list
  private void postRoute(Router router) {
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      Date d = new Date();
      DateFormat mediumDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
      String sql = String.format("INSERT INTO service (url, creationDate, name) SELECT '%s', '%s', NULL " +
                                 "WHERE NOT EXISTS (SELECT * FROM service WHERE url = '%s')",
                   jsonBody.getString("url"), mediumDateFormat.format(d), jsonBody.getString("url"));
      connector.query(sql).setHandler(done -> {
        if(done.succeeded()){
          System.out.println("Service added");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          System.out.println("Error adding service");
          done.cause().printStackTrace();
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("FAIL");
        }
      });
    });
  }

  // Remove URL from the services list
  private void deleteRoute(Router router) {
    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      String sql = String.format("DELETE FROM service WHERE url = '%s'", jsonBody.getString("url"));
      Service service = new Service(jsonBody.getString("url"),
                                    jsonBody.getString("date"),
                                    jsonBody.getString("name"));

      services.remove(service);
      connector.query(sql).setHandler(done -> {
        if(done.succeeded()){
          System.out.println("Service removed");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          System.out.println("Error removing service");
          done.cause().printStackTrace();
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("FAIL");
        }
      });
    });
  }

  // Update a Service name from the services list
  private void putRoute(Router router) {
    router.put("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      String sql = String.format("UPDATE service SET name = '%s' WHERE url = '%s'",
              jsonBody.getString("name"), jsonBody.getString("url"));
      connector.query(sql).setHandler(done -> {
        if(done.succeeded()){
          System.out.println("Service updated");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          System.out.println("Error updating service");
          done.cause().printStackTrace();
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("FAIL");
        }
      });
    });
  }

  private void insertKry() {
    Date d = new Date();
    DateFormat mediumDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    String sql = String.format("INSERT INTO service (url, creationDate, name) SELECT 'https://www.kry.se', '%s', 'Kry' " +
                    "WHERE NOT EXISTS (SELECT * FROM service WHERE url = 'https://www.kry.se')",
                    mediumDateFormat.format(d));
    connector.query(sql).setHandler(done -> {
      if(done.succeeded()){
        System.out.println("Kry service added");
      } else {
        System.out.println("Error adding Kry service");
        done.cause().printStackTrace();
      }
    });
  }
}



