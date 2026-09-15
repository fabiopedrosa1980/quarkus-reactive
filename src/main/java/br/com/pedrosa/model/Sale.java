package br.com.pedrosa.model;

import java.math.BigDecimal;

public record Sale(String productName , int quantity, BigDecimal total) {
}
