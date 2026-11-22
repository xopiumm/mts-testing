package ru.mts.itmo.aqa;

import io.gatling.javaapi.core.Simulation;
import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class ProductsSimulation extends Simulation {
    public ProductsSimulation() {
        int users = Integer.getInteger("users", 10);
        int ramp = Integer.getInteger("ramp", 10);
        int duration = Integer.getInteger("duration", 40);
        var protocol = http.baseUrl("http://127.0.0.1:" + System.getProperty("port", "8087"));
        var scenario = scenario("Products users").forever().on(
            feed(csv("products.csv").circular()),
            pause(Duration.ofMillis(200)),
            exec(http("GET product").get("/api/products/#{id}")
                .check(status().is(200), jsonPath("$.id").ofString().is(session -> session.getString("id"))))
        );
        setUp(scenario.injectClosed(
            rampConcurrentUsers(0).to(users).during(ramp),
            constantConcurrentUsers(users).during(duration - ramp)
        )).protocols(protocol).maxDuration(Duration.ofSeconds(duration))
          .assertions(global().failedRequests().count().is(0L));
    }
}
