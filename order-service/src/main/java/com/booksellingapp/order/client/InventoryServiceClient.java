package com.booksellingapp.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    @PostMapping("/api/inventory/check-stock")
        StockCheckResponse checkStock(@RequestBody List<StockItem> requests);

    @PostMapping("/api/inventory/reserve-stock")
        ReservationResponse reserveStock(@RequestParam String orderId, @RequestBody List<ReservationItem> requests);

    @PostMapping("/api/inventory/release-stock")
        ReleaseResponse releaseStock(@RequestParam String orderId, @RequestBody List<ReleaseItem> requests);

    record StockItem(
            String productId,
            Integer quantity
    ) {}

    record StockCheckResponse(
            boolean allItemsAvailable,
            List<StockCheckDetail> details
    ) {}

    record StockCheckDetail(
            String productId,
            boolean available,
            Integer availableQuantity
    ) {}

    record ReservationItem(
            String productId,
            Integer quantity
    ) {}

    record ReleaseItem(
            String productId,
            Integer quantity
    ) {}

    record ReservationDetail(String productId, Integer quantity, Integer quantityAvailable, Integer quantityReserved) {}
    record ReservationResponse(String orderId, String status, List<ReservationDetail> details) {}

    record ReleaseDetail(String productId, Integer quantity, Integer quantityAvailable) {}
    record ReleaseResponse(String orderId, String status, List<ReleaseDetail> details) {}
}
