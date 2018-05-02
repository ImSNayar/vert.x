package io.vertx.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class TestVerticle extends AbstractVerticle {


    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/kickoff/springboot").handler(this::boot);
        router.get("/kickoff/vertx").handler(this::vertx);

        System.out.println("STARTING VERT.X TEST CLIENT");
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8083);
        System.out.println("STARTED VERT.X TEST CLIENT");
    }

    private void boot(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        System.out.println("Received request for boot");


        for (int i = 0; i < 100; i++) {
            WebClient client = WebClient.create(vertx);
            System.out.println("Starting boot request: " + i);
            client.get(8080, "localhost", "/demo/test").send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
//                    System.out.println("Got Spring boot HTTP response with status " + response.statusCode() + " with data " +
//                            response.body().toString("ISO-8859-1"));
                } else {
                    ar.cause().printStackTrace();
                }
            });
            System.out.println("Received boot response: " + i);
        }
        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end("{\"Response\": \"Hello World Spring boot\"}");
    }


    private void vertx(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        System.out.println("Received request for vertx");

        for (int i = 0; i < 100; i++) {
            WebClient client = WebClient.create(vertx);
            System.out.println("Starting vertx request: " + i);
            client.get(8081, "localhost", "/demo/test").send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
//                    System.out.println("Got vert.x HTTP response with status " + response.statusCode() + " with data " +
//                            response.body().toString("ISO-8859-1"));
                } else {
                    ar.cause().printStackTrace();
                }
            });
            System.out.println("Starting vertx request: " + i);
        }
        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                 .end("{\"Response\": \"Hello World vertx\"}");
    }
}
