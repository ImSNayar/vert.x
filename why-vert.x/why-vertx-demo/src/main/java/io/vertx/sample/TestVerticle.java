package io.vertx.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.TimeUnit;

public class TestVerticle extends AbstractVerticle {


    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/demo/test").blockingHandler(this::test, false);
        router.get("/demo/hello").handler(this::hello);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8081);
    }

    private void test(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        int i = 0;
        long startTime = System.nanoTime();
        try {
            System.out.println(Thread.currentThread().getName() + ", Received Req. " + TimeUnit.SECONDS.convert(
                    System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
            Thread.sleep(30000L);
            System.out.println(Thread.currentThread().getName() + ", Processed Req. " + TimeUnit.SECONDS.convert(
                    System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/text").end(Thread.currentThread()
                .getName());
    }


    private void hello(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        System.err.println(Thread.currentThread().getName());
        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end("{\"Text\": \"Hello World from Vert.x\"}");
    }
}
