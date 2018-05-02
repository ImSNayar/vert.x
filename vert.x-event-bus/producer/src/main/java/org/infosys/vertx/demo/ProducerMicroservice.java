package org.infosys.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ProducerMicroservice extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/").handler(this::service);
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(9090);
    }

    private void service(RoutingContext routingContext) {
        vertx.eventBus().send("service_a", "request_body", reply -> {
            if (reply.succeeded()) {
                System.out.println("Received: " + reply.result().body());
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(String.valueOf(reply.result().body()));
            } else {
                reply.cause().printStackTrace();
            }
        });
    }

}
