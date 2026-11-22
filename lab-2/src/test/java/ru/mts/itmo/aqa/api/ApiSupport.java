package ru.mts.itmo.aqa.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

abstract class ApiSupport {
    static final String REST_BASE = System.getProperty("rest.base", "https://playground.nileslabs.com/api/v1");
    static final String GRAPHQL_URL = System.getProperty("graphql.url", "https://graphql.postman-echo.com/graphql");

    @BeforeAll
    static void logFailures() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    static RequestSpecification jsonRequest() {
        return new RequestSpecBuilder()
                .setAccept("application/json")
                .setContentType("application/json")
                .build();
    }

    static RequestSpecification restRequest(String identity) {
        RequestSpecification request = RestAssured.given().spec(jsonRequest()).baseUri(REST_BASE);
        if (identity != null && !identity.isBlank()) {
            request.header("X-Playground-Identity", identity);
        }
        return request;
    }

    static String setting(String name, String environment, String fallback) {
        String property = System.getProperty(name);
        if (property != null && !property.isBlank()) return property;
        String value = System.getenv(environment);
        return value != null && !value.isBlank() ? value : fallback;
    }
}
