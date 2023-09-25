package com.acozac.resources;

import java.net.URI;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import com.acozac.model.request.TransactionRequest;
import com.acozac.model.request.WithdrawalRequest;
import com.acozac.services.OperationCache;
import com.acozac.services.TransactionService;
import com.acozac.services.WithdrawalServiceProxy;
import com.acozac.util.ToOperationConvertor;

@Path("/money/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Money Transfers", description = "Operations related to money transfers")
public class MoneyTransferResource
{
    @Inject
    private TransactionService transactionService;
    @Inject
    private WithdrawalServiceProxy withdrawalServiceProxy;
    @Inject
    private OperationCache operationCache;

    @POST
    @Path("/transactions")
    @Operation(summary = "Perform a transaction", description = "Performs a money transfer from one user to another.")
    @APIResponse(responseCode = "201", description = "Transaction created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @RequestBody(description = "Transaction data to create", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransactionRequest.class)))
    @Transactional
    public Response performTransaction(TransactionRequest transactionRequest, @Context UriInfo uriInfo)
    {
        com.acozac.model.Operation result = transactionService.performOperation(ToOperationConvertor.from(transactionRequest));
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(result.getOperationId().toString());
        return Response.created(builder.build()).build();
    }

    @POST
    @Path("/withdrawals")
    @Operation(summary = "Initiate a withdrawal", description = "Initiate a withdrawal to an external address.")
    @APIResponse(responseCode = "201", description = "Withdrawal initiated", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @RequestBody(description = "withdrawal information", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WithdrawalRequest.class)))
    @Transactional
    public Response initiateWithdrawal(WithdrawalRequest withdrawalRequest, @Context UriInfo uriInfo)
    {
        com.acozac.model.Operation result = withdrawalServiceProxy.performOperation(ToOperationConvertor.from(withdrawalRequest));
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(result.getOperationId().toString());
        return Response.created(builder.build()).build();
    }

    @GET
    @Path("/operation/status/{operationId}")
    @Operation(summary = "Get operation by ID", description = "Retrieves operation status by transaction ID.")
    @APIResponse(responseCode = "200", description = "operation found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Operation.class)))
    @APIResponse(responseCode = "404", description = "operation not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @Parameter(name = "operationId", description = "operation ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = SchemaType.STRING))
    public Response getStatus(@RestPath UUID operationId)
    {
        com.acozac.model.Operation operation = operationCache.get(operationId);
        if (operation != null)
        {
            return Response.ok(operation.getStatus()).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Operation not found").build();
        }
    }
}
