package com.booksellingapp.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    @PostMapping("/api/inventory/check-stock")
    StockCheckResponse checkStock(@RequestBody StockCheckRequest request);

    @PostMapping("/api/inventory/reserve-stock")
    void reserveStock(@RequestBody ReserveStockRequest request);

    @PostMapping("/api/inventory/release-stock")
    void releaseStock(@RequestBody ReleaseStockRequest request);

    record StockCheckRequest(
            List<StockItem> items
    ) {}

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

    record ReserveStockRequest(
            String orderId,
            List<ReservationItem> items
    ) {}

    record ReservationItem(
            String productId,
            Integer quantity
    ) {}

    record ReleaseStockRequest(
            String orderId,
            List<ReleaseItem> items
    ) {}

    record ReleaseItem(
            String productId,
            Integer quantity
    ) {}
}
