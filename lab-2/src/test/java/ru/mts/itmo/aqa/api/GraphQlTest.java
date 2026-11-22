package ru.mts.itmo.aqa.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class GraphQlTest extends ApiSupport {
    private Response execute(String query) {
        Response response = given().spec(jsonRequest())
                .body(Map.of("query", query))
                .post(GRAPHQL_URL);
        response.then().statusCode(200);
        assertNull(response.jsonPath().get("errors"), "GraphQL errors must be absent");
        assertNotNull(response.jsonPath().get("data"), "GraphQL data must be present");
        return response;
    }

    @Test
    void helloQueryReturnsRequestedName() {
        Response response = execute("query Hello { hello(person: { name: \"Ivan\", age: 35 }) }");
        assertEquals("Hello Ivan", response.jsonPath().getString("data.hello"));
    }

    @Test
    void createPersonMutationReturnsCreatedFields() {
        Response response = execute("mutation CreatePerson { createPerson(person: { name: \"Student\", age: 20 }) { id name age } }");
        String id = response.jsonPath().getString("data.createPerson.id");
        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals("Student", response.jsonPath().getString("data.createPerson.name"));
        assertEquals(20, response.jsonPath().getInt("data.createPerson.age"));
    }
}
