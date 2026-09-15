package br.com.pedrosa.resource;

import br.com.pedrosa.resource.request.SaleRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
@Path("/sales")
public class SaleResource {

    @Channel("sale-requests")
    Emitter<SaleRequest> saleRequestEmitter;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createSale(SaleRequest saleRequest) {
        return Uni.createFrom()
                .completionStage(saleRequestEmitter.send(saleRequest))
                .onItem().transform(_ -> Response.status(Response.Status.CREATED).build())
                .onFailure().transform(throwable ->
                        new InternalServerErrorException(throwable.getMessage(), throwable));
    }
}
