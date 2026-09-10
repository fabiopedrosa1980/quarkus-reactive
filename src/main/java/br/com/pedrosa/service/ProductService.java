package br.com.pedrosa.service;


import br.com.pedrosa.entity.ProductEntity;
import br.com.pedrosa.request.ProductRequest;
import br.com.pedrosa.response.ProductResponse;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ProductService {

    @WithSession
    public Uni<List<ProductResponse>> getProducts() {
        return ProductEntity.<ProductEntity>listAll()
                .map(products -> products.stream()
                        .map(ProductResponse::fromEntity)
                        .toList());
    }

    @WithSession
    public Uni<ProductResponse> findById(Long id) {
        return ProductEntity.<ProductEntity>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Product not found"))
                .map(ProductResponse::fromEntity);
    }

    @WithTransaction
    public Uni<ProductResponse> create(ProductRequest productRequest) {
        ProductEntity entity = productRequest.toEntity();
        return entity.persist()
                .map(_ -> ProductResponse.fromEntity(entity));
    }

    @WithTransaction
    public Uni<ProductResponse> update(Long id, ProductRequest productRequest) {
        return ProductEntity.<ProductEntity>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Product not found"))
                .map(entity -> {
                    entity.fromRequest(productRequest);
                    return entity;
                })
                .map(ProductResponse::fromEntity);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return ProductEntity.<ProductEntity>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Product not found"))
                .call(PanacheEntityBase::delete).replaceWithVoid();
    }
}
