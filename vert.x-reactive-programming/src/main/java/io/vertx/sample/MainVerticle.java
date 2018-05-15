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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class MainVerticle extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private WebClient client;
    private Random random = new Random();


    private Future<Void> startHttpServer() {

        Future<Void> future = Future.future();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/demo/kickoff").handler(this::get);
        client = WebClient.create(vertx);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(7082, ar -> {   // <6>
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port 7082");
                        future.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        future.fail(ar.cause());
                    }
                });


        return future;
    }


    private void get(RoutingContext routingContext) {
        int l = random.nextInt();
        JsonObject jsonObject = new JsonObject().put("Id", l).put("Name", String.valueOf(l));
        client.post(7081, "localhost", "/demo/employee").sendJson(jsonObject, event -> {
            System.out.println("Creating Employee:" + String.valueOf(l));
            if (event.succeeded()) {
                int l2 = random.nextInt();
                JsonObject jsonObject2 = new JsonObject().put("Id", l2).put("Name", String.valueOf(l2));
                System.out.println("Creating Employee:" + l2);
                client.post(7081, "localhost", "/demo/employee").sendJson(jsonObject2, event2 -> {
                    if (event2.succeeded()) {
                        int l3 = random.nextInt();
                        JsonObject jsonObject3 = new JsonObject().put("Id", l3).put("Name", String.valueOf(l3));
                        System.out.println("Creating Employee:" + l3);
                        client.post(7081, "localhost", "/demo/employee").sendJson(jsonObject3, event3 -> {
                            if (event2.succeeded()) {
                                System.out.println("Successfully called three services");
                                routingContext.response().setStatusCode(200).end();
                            }
                        });
                    }
                });
            }
        });

    }


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startHttpServer().setHandler(startFuture.completer());
        vertx.deployVerticle("io.vertx.sample.RxMainVerticle", res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
            }
        });
    }

}
