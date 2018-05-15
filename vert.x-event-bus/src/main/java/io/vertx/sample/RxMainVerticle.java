/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.sample;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.util.Random;


public class RxMainVerticle extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(RxMainVerticle.class);

    private Random random = new Random();


    private Future<Void> startHttpServer() {

        Future<Void> future = Future.future();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/demo/kickoffrx").handler(this::getrx);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(7085, ar -> {   // <6>
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port 7085");
                        future.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        future.fail(ar.cause());
                    }
                });


        return future;
    }

    private void getrx(RoutingContext routingContext) {
        System.out.println(routingContext);

        int l = random.nextInt();
        JsonObject jsonObject = new JsonObject().put("Id", l).put("Name", String.valueOf(l));
        Single<Message<Object>> obs1 = vertx.eventBus().rxSend("db.post", jsonObject);
        System.out.println("Send Message 1");

        l = random.nextInt();
        jsonObject = new JsonObject().put("Id", l).put("Name", String.valueOf(l));
        Single<Message<Object>> obs2 = vertx.eventBus().rxSend("db.post", jsonObject);
        System.out.println("Send Message 2");


        l = random.nextInt();
        jsonObject = new JsonObject().put("Id", l).put("Name", String.valueOf(l));
        Single<Message<Object>> obs3 = vertx.eventBus().rxSend("db.post", jsonObject);
        System.out.println("Send Message 3");

        Single.zip(obs1, obs2, obs3, (val1, val2, val3) -> {
            System.out.println(val1.body() + ", " + val2.body() + ", " + val3.body());
            return "Successfully called three services";
        }).subscribe(
                x -> {
                    System.out.println(x);
                    routingContext.response().end(x);
                },
                t -> {
                    routingContext.response().setStatusCode(500).end();
                });
    }


    @Override
    public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
        startHttpServer();
//        DeploymentOptions options = new DeploymentOptions().setInstances(16);
//        vertx.deployVerticle("io.vertx.sample.MainVerticle", options, event -> {
//            System.out.println("Verticle Started: " + event.result());
//        });
    }

}
