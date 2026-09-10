package br.com.pedrosa.request;


import br.com.pedrosa.entity.ProductEntity;

import java.math.BigDecimal;

public record ProductRequest(String name, BigDecimal price) {

    public ProductEntity toEntity() {
        return new ProductEntity(this.name,this.price);
    }
}
