package br.com.pedrosa.entity;

import br.com.pedrosa.resource.request.ProductRequest;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Product")
public class ProductEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "products_seq", sequenceName = "products_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    public Long id;
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
