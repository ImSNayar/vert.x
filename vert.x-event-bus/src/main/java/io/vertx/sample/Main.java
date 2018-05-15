package io.vertx.sample;


import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;

public class Main {
    public static void main(String[] args) {
        ClusterManager mgr = new IgniteClusterManager();
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr);
//                .setClustered(true)
//
//                // If there are multiple n/w interfaces, tell Vertx which one to use.
//                .setClusterHost("127.0.0.1")
//
//                // Vertx start a separate HTTP process on this port for event-bus communication
//                // via TCP, and Vertx use this port to send event-bus communication via TCP.
//                .setClusterPort(41233);

        Vertx.rxClusteredVertx(options).subscribe(vertx -> {
            System.out.println("Hazelcast started good.." + vertx);
        }, ex -> ex.printStackTrace());

//        DeploymentOptions options = new DeploymentOptions().setInstances(16);
        Vertx.vertx().deployVerticle("io.vertx.sample.MainVerticle", event -> {
            System.out.println("Verticle Started: " + event.result());
        });
    }


}
