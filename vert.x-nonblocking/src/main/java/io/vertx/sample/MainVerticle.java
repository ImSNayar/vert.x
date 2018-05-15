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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle extends AbstractVerticle {

    private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Employee (Id integer primary key, Name varchar(255) unique)";


    private JDBCClient dbClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private Future<Void> prepareDatabase() {
        Future<Void> future = Future.future();

        dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:mem:employee")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                future.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
                    connection.close();
                    if (create.failed()) {
                        LOGGER.error("Database preparation error", create.cause());
                        future.fail(create.cause());
                    } else {
                        future.complete();
                    }
                });
            }
        });

        return future;
    }

    private Future<Void> startHttpServer() {

        Future<Void> future = Future.future();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/demo/employee").handler(this::get);
        router.post("/demo/employee").handler(this::post);
        router.delete("/demo/employee").handler(this::delete);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(7081, ar -> {   // <6>
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port 7081");
                        future.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        future.fail(ar.cause());
                    }
                });


        return future;
    }

    private void delete(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.request().response();
        String name = routingContext.request().getParam("Name");

        JsonArray params = new JsonArray().add(name);
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                response.setStatusCode(500);
                response.end();
            } else {
                SQLConnection connection = ar.result();
                connection.updateWithParams("delete from Employee where Name = ? ", params, result -> {
                    connection.close();
                    if (result.failed()) {
                        LOGGER.error("Database preparation error", result.cause());
                        response.setStatusCode(500);
                        response.end();
                    } else {
                        response.setStatusCode(204);
                        response.end();
                    }
                });
            }
        });}


    private void post(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.request().response();
        JsonObject jsonObject = routingContext.getBodyAsJson();
        System.out.println(Thread.currentThread() + ", " + routingContext.getBodyAsString());

        JsonArray params = new JsonArray().add(jsonObject.getInteger("Id"));
        params.add(jsonObject.getString("Name"));
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                response.setStatusCode(500);
                response.end();
            } else {
                SQLConnection connection = ar.result();
                connection.updateWithParams("insert into Employee (Id, Name) values (?, ?) ", params, result -> {
                    connection.close();
                    if (result.failed()) {
                        LOGGER.error("Database preparation error", result.cause());
                        response.setStatusCode(500);
                        response.end();
                    } else {
                        response.setStatusCode(204);
                        response.end();
                    }
                });
            }
        });
    }

    private void get(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.request().response();
        String name = routingContext.request().getParam("Name");

        JsonArray params = new JsonArray().add(name);
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                response.setStatusCode(500);
                response.end();
            } else {
                SQLConnection connection = ar.result();
                connection.queryWithParams("select Id, Name from Employee where Name = ? ", params, fetch -> {
                    connection.close();
                    if (fetch.failed()) {
                        LOGGER.error("Database preparation error", fetch.cause());
                        response.setStatusCode(500);
                        response.end();
                    } else {
                        ResultSet rs = fetch.result();
                        JsonArray row = rs.getResults().get(0);
                        JsonObject map = new JsonObject();
                        map.put("id", String.valueOf(row.getInteger(0)));
                        map.put("name", row.getString(1));
                        String resStr = Json.encodePrettily(map);
                        response.headers()
                                .add("Content-Length", String.valueOf(resStr.length()))
                                .add("Content-Type", "application/json");
                        response.write(resStr);

                        response.end();
                    }
                });
            }
        });
    }


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
        steps.setHandler(startFuture.completer());
    }

}
