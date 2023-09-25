package com.acozac.resources;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import com.acozac.model.request.TransactionRequest;
import com.acozac.model.request.WithdrawalRequest;

@QuarkusTest
class MoneyTransferResourceTest
{
    @Test
    void given_transactionRequest_shouldPerformTransaction()
    {
        TransactionRequest transactionRequest = new TransactionRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("255"));

        given()
            .body(transactionRequest)
            .contentType(JSON)
            .accept(JSON)
            .when()
            .post("/money/v1/transactions")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void given_withdrawalRequest_shouldPerformTransaction()
    {
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest(UUID.randomUUID(), UUID.randomUUID().toString(), new BigDecimal("255"));

        given()
            .body(withdrawalRequest)
            .contentType(JSON)
            .accept(JSON)
            .when()
            .post("/money/v1/withdrawals")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void given_OperationIdentifier_shouldGetOperationStatus()
    {
        TransactionRequest transactionRequest = new TransactionRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("255"));

        io.restassured.response.Response response = given()
            .body(transactionRequest)
            .contentType(JSON)
            .accept(JSON)
            .when()
            .post("/money/v1/transactions")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract()
            .response();

        // Extract the 'Location' header from the response
        String locationHeader = response.header("Location");

        // Extract the UUID from the 'Location' header
        String[] parts = locationHeader.split("/");
        String uuid = parts[parts.length - 1];

        given()
            .pathParam("operationId", uuid)
            .when()
            .get("/money/v1/operation/status/{operationId}")
            .then()
            .statusCode(Response.Status.OK.getStatusCode());
    }
}
