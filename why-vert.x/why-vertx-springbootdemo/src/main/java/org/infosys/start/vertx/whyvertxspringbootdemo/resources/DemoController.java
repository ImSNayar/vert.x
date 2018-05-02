package org.infosys.start.vertx.whyvertxspringbootdemo.resources;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/demo")
public class DemoController {
    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public String test() {
        long startTime = System.nanoTime();
        try {
            System.out.println(Thread.currentThread().getName() + ", Received Req. " + TimeUnit.SECONDS.convert(
                    System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
            Thread.sleep(300000L);
            System.out.println(Thread.currentThread().getName() + ", Processed Req. " + TimeUnit.SECONDS.convert(
                    System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Thread.currentThread().getName();
    }

    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello world!";
    }
}
