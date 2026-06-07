package com.booksellingapp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String productCode;
    private String title;
    private String description;
    private String author;
    private String isbn;
    private BigDecimal price;
    private String category;
    private Boolean available;
}
