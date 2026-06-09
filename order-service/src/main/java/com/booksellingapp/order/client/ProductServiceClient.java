package com.booksellingapp.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/code/{productCode}")
    ProductResponse getProductByCode(@PathVariable String productCode);

    @GetMapping("/api/products/check-availability/{productCode}")
    boolean checkAvailability(@PathVariable String productCode);

    record ProductResponse(
            Long id,
            String productCode,
            String title,
            String description,
            String author,
            String isbn,
            java.math.BigDecimal price,
            String category,
            Boolean available
    ) {}
}