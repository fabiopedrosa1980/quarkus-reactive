package br.com.pedrosa.request;


import br.com.pedrosa.entity.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message="Name may not be blank")
        String name,
        @NotNull(message = "Price must be not be null")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price) {

    public ProductEntity toEntity() {
        return new ProductEntity(this.name,this.price);
    }
}
