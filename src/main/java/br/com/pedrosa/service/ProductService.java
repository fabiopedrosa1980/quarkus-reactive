package br.com.pedrosa.service;

import br.com.pedrosa.entity.ProductEntity;
import br.com.pedrosa.resource.request.ProductRequest;
import br.com.pedrosa.resource.response.PaginationResponse;
import br.com.pedrosa.resource.response.ProductResponse;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ProductService {

    @WithSession
    public Uni<PaginationResponse<ProductResponse>> getProducts(int pageIndex, int pageSize) {
        PanacheQuery<ProductEntity> query = ProductEntity.<ProductEntity>findAll(Sort.by("name"))
                .page(Page.of(pageIndex, pageSize));

        return Uni.combine().all().unis(query.count(), query.list())
                .with((total, entities) -> new PaginationResponse<>(
                        entities.stream().map(ProductResponse::fromEntity).toList(),
                        total,
                        pageSize,
                        pageIndex + 1
                ));
    }

    @WithSession
    public Uni<ProductResponse> findById(Long id) {
        return ProductEntity.<ProductEntity>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Produto nao encontrado"))
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
                .onItem().ifNull().failWith(() -> new NotFoundException("Produto nao encontrado"))
                .map(entity -> {
                    entity.fromRequest(productRequest);
                    return entity;
                })
                .map(ProductResponse::fromEntity);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return ProductEntity.<ProductEntity>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Produto nao encontrado"))
                .call(PanacheEntityBase::delete).replaceWithVoid();
    }
}
