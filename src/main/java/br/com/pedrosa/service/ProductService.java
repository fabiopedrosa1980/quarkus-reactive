package br.com.pedrosa.service;

import br.com.pedrosa.entity.ProductEntity;
import br.com.pedrosa.resource.request.ProductRequest;
import br.com.pedrosa.resource.response.PagedResponse;
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

import java.util.List;

@ApplicationScoped
public class ProductService {

    @WithSession
    public Uni<PagedResponse<ProductResponse>> getProducts(int pageIndex, int pageSize) {

        PanacheQuery<ProductEntity> query = ProductEntity.<ProductEntity>findAll(Sort.by("name"))
                .page(Page.of(pageIndex, pageSize));

        return Uni.combine().all().unis(query.count(), query.list())
                .with((total, entities) -> toPagedResponse(entities, total, pageIndex + 1, pageSize));
    }

    private PagedResponse<ProductResponse> toPagedResponse(
            List<ProductEntity> entities, long total, int pageIndex, int pageSize) {
        List<ProductResponse> content = entities.stream()
                .map(ProductResponse::fromEntity)
                .toList();
        return new PagedResponse<>(content, total, pageSize, pageIndex);
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
