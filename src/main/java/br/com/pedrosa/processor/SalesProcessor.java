package br.com.pedrosa.processor;

import br.com.pedrosa.entity.ProductEntity;
import br.com.pedrosa.model.Sale;
import br.com.pedrosa.resource.request.SaleRequest;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.quarkus.logging.Log;

import java.math.BigDecimal;

@ApplicationScoped
public class SalesProcessor {

    @Incoming("sales")
    @Outgoing("sales-received")
    @WithSession
    public Uni<Sale> process(SaleRequest saleRequest) {
        Log.info("Consumindo o topico de sale" + saleRequest);
        return ProductEntity.<ProductEntity>findById(saleRequest.idProduct())
                .onItem().ifNull().failWith(() -> new NotFoundException("Produto nao encontrado"))
                .onItem().transform(product -> new Sale(
                        product.name,
                        saleRequest.quantity(),
                        product.price.multiply(BigDecimal.valueOf(saleRequest.quantity()))
                ));
    }
}
