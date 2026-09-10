package br.com.pedrosa.entity;

import br.com.pedrosa.request.ProductRequest;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class ProductEntity extends PanacheEntity {
    public String name;
    public BigDecimal price;

    public ProductEntity(String name,BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public void fromRequest(ProductRequest productRequest){
        this.name = productRequest.name();
        this.price = productRequest.price();

    }
}
