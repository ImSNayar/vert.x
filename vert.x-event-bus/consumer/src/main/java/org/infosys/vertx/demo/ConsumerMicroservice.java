package org.infosys.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Set;

public class ConsumerMicroservice extends AbstractVerticle {

    @Override
    public void start() {
        io.vertx.ext.web.Router router = Router.router(vertx);


        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("service_a"))
                .addOutboundPermitted(new PermittedOptions().setAddress("service_a"));
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(9091);

        vertx.eventBus().<String>consumer("service_a", message -> {
            System.out.println("Received Message");
            JsonObject json = new JsonObject().put("service-provider", this.toString());
            json.put("request", message.body());
            message.reply(json.put("time", DateTimeFormatter.ofPattern("MMM d yyyy  hh:mm:ss a")
                    .format(LocalDateTime.now())));
            System.out.println("Processed Message");
        });
    }

}
