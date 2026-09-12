package br.com.pedrosa.resource;

import br.com.pedrosa.resource.request.ProductRequest;
import br.com.pedrosa.resource.response.PagedResponse;
import br.com.pedrosa.resource.response.ProductResponse;
import br.com.pedrosa.service.ProductService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import static org.jboss.resteasy.reactive.RestResponse.Status.CREATED;
import static org.jboss.resteasy.reactive.RestResponse.Status.OK;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT;

@Path("api/v1/products")
@ApplicationScoped
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    @WithSession
    public Uni<PagedResponse<ProductResponse>> listProducts(
            @QueryParam("page") @DefaultValue("1") @Min(value = 1, message = "page deve ser maior que 0") int page,
            @QueryParam("size") @DefaultValue("20") @Min(value = 1, message = "size deve ser maior que 0") int size) {
        int pageIndex = page - 1;
        return productService.getProducts(pageIndex, size);
    }

    @GET
    @Path("/{id}")
    public Uni<RestResponse<ProductResponse>> findById(@PathParam("id") Long id) {
        return productService.findById(id)
                .map(productResponse -> RestResponse.status(OK, productResponse));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ProductResponse>> create(@Valid ProductRequest productRequest) {
        return productService.create(productRequest)
                .map(productResponse -> RestResponse.status(CREATED, productResponse));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ProductResponse>> update(@PathParam("id") Long id, @Valid ProductRequest productRequest) {
        return productService.update(id, productRequest)
                .map(productResponse -> RestResponse.status(OK, productResponse));
    }

    @DELETE
    @Path("/{id}")
    public Uni<RestResponse<Void>> delete(@PathParam("id") Long id) {
        return productService.delete(id)
                .map(_ -> RestResponse.status(NO_CONTENT));
    }
}
