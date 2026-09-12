package br.com.pedrosa.resource.response;

import br.com.pedrosa.entity.ProductEntity;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {
    public static ProductResponse fromEntity(ProductEntity productEntity){
        return new ProductResponse(productEntity.id,productEntity.name, productEntity.price);
    }
}
