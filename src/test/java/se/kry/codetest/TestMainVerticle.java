package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Start a web server on localhost responding to path /service on port 8080")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .get(8080, "::1", "/service")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          assertNotEquals(0, body.size());
          testContext.completeNow();
        }));
  }

    @Test
    @DisplayName("Test the addition of a new service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_server(Vertx vertx, VertxTestContext testContext) {
      JsonObject obj = new JsonObject()
              .put("url", "http://test.com")
              .put("name", "Test")
              .put("creationDate", "Test");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(obj, ar -> {
                    testContext.verify(() -> {
                        assertTrue(ar.succeeded());
                        testContext.completeNow();
                    });
                });
    }

    @Test
    @DisplayName("Test the deletion of a service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_server(Vertx vertx, VertxTestContext testContext) {
        JsonObject obj = new JsonObject()
                .put("url", "http://test.com")
                .put("name", "Test")
                .put("date", "Test");
        WebClient.create(vertx)
                .delete(8080, "::1", "/service")
                .sendJsonObject(obj, ar -> {
                    testContext.verify(() -> {
                        assertTrue(ar.succeeded());
                        testContext.completeNow();
                    });
                });
    }

}
