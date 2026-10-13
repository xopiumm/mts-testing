package ru.mts.itmo.aqa.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestScenarioTest extends ApiSupport {
    @Test
    void createdPostCanBeUpdatedAndReadWithoutLosingBody() {
        String title = "API homework " + UUID.randomUUID();
        String updatedTitle = title + " updated";
        String body = "Body must survive PATCH";
        String identity = restRequest(null).get("/posts?_limit=1")
                .then().statusCode(200).extract().header("X-Playground-Identity");
        assertNotNull(identity, "Playground must provide a session identity");

        Response created = restRequest(identity)
                .body(Map.of("title", title, "body", body, "user_id", 1))
                .post("/posts");
        created.then().statusCode(201);
        String id = created.jsonPath().getString("id");
        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals(title, created.jsonPath().getString("title"));
        assertEquals(body, created.jsonPath().getString("body"));

        Response patched = restRequest(identity)
                .body(Map.of("title", updatedTitle))
                .patch("/posts/{id}", id);
        patched.then().statusCode(200);
        assertEquals(id, patched.jsonPath().getString("id"));
        assertEquals(updatedTitle, patched.jsonPath().getString("title"));
        assertEquals(body, patched.jsonPath().getString("body"));

        Response read = restRequest(identity).get("/posts/{id}", id);
        read.then().statusCode(200);
        assertEquals(id, read.jsonPath().getString("id"));
        assertEquals(updatedTitle, read.jsonPath().getString("title"));
        assertEquals(body, read.jsonPath().getString("body"));
        assertEquals(1, read.jsonPath().getInt("user_id"));
    }
}
