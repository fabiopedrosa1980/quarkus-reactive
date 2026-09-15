package br.com.pedrosa.resource;

import br.com.pedrosa.model.Sale;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;

@ApplicationScoped
@Path("/api/v1/sales-panel")
public class SalePanelResource {

    @Inject
    @Channel("sales-received")
    Multi<Sale> sales;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Sale> stream() {
        return sales;
    }
}
