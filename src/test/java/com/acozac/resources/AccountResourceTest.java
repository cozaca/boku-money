package com.acozac.resources;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;

@QuarkusTest
public class AccountResourceTest
{

    @Test
    void shoudGetAllAccounts()
    {
        given()
            .when()
            .get("/money/v1/accounts/")
            .then()
            .statusCode(Response.Status.OK.getStatusCode());
    }
}
