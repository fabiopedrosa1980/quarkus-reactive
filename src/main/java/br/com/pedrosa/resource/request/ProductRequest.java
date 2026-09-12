package br.com.pedrosa.resource.request;


import br.com.pedrosa.entity.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message="Nome não pode estar em branco")
        String name,
        @NotNull(message = "Preco não pode ser null")
        @Positive(message = "Preco precisar ser maior que 0")
        BigDecimal price) {

    public ProductEntity toEntity() {
        return new ProductEntity(this.name,this.price);
    }
}
