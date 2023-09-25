package com.acozac.resources;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.acozac.services.AccountService;

@Path("/money/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Accounts management", description = "Operations related to accounts management")
public class AccountResource
{
    @Inject
    private AccountService accountService;

    @GET
    @Path("/accounts/")
    @Operation(summary = "Get existing accounts", description = "Retrieves existing accounts!")
    @APIResponse(responseCode = "200", description = "accounts found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = List.class)))
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getAccounts()
    {
        return Response.ok(accountService.getAll()).build();
    }
}
