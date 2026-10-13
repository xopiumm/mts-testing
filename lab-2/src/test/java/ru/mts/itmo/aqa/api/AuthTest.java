package ru.mts.itmo.aqa.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest extends ApiSupport {
    @Test
    void loginTokenGrantsAccessToCurrentUser() {
        String password = setting("auth.password", "PLAYGROUND_PASSWORD", "Password@123");
        Response login = restRequest(null)
                .body(Map.of("username", "Bret", "password", password))
                .post("/auth/login");
        login.then().statusCode(200);
        String token = login.jsonPath().getString("access_token");
        assertNotNull(token);
        assertFalse(token.isBlank());

        Response me = restRequest(null).auth().oauth2(token).get("/auth/me");
        me.then().statusCode(200);
        assertEquals("bret", me.jsonPath().getString("username").toLowerCase());
        assertEquals(1, me.jsonPath().getInt("id"));
        assertFalse(me.jsonPath().getString("email").isBlank());
    }

    @Test
    void missingTokenCannotAccessCurrentUser() {
        Response me = restRequest(null).get("/auth/me");
        me.then().statusCode(401);
        assertEquals(401, me.jsonPath().getInt("status"));
        assertTrue(me.jsonPath().getString("message").toLowerCase().contains("token"));
    }

    @Test
    void invalidTokenCannotAccessCurrentUser() {
        Response me = restRequest(null).header("Authorization", "Bearer invalid-token")
                .get("/auth/me");
        me.then().statusCode(401);
        assertEquals(401, me.jsonPath().getInt("status"));
        assertFalse(me.jsonPath().getString("message").isBlank());
    }
}
