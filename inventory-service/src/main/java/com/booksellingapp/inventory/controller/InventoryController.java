package com.booksellingapp.inventory.controller;

import com.booksellingapp.inventory.dto.InventoryDTO;
import com.booksellingapp.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Check stock availability
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/check-stock")
    public ResponseEntity<InventoryService.StockCheckResponse> checkStock(
            @RequestBody List<InventoryService.StockCheckRequest> requests) {
        log.info("POST request: /api/inventory/check-stock");
        InventoryService.StockCheckResponse response = inventoryService.checkStock(requests);
        return ResponseEntity.ok(response);
    }

    /**
     * Reserve stock
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/reserve-stock")
    public ResponseEntity<InventoryService.ReservationResponse> reserveStock(
            @RequestParam String orderId,
            @RequestBody List<InventoryService.ReservationRequest> requests) {
        log.info("POST request: /api/inventory/reserve-stock for orderId: {}", orderId);
        InventoryService.ReservationResponse response = inventoryService.reserveStock(orderId, requests);
        return ResponseEntity.ok(response);
    }

    /**
     * Release reserved stock
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/release-stock")
    public ResponseEntity<InventoryService.ReleaseResponse> releaseStock(
            @RequestParam String orderId,
            @RequestBody List<InventoryService.ReleaseRequest> requests) {
        log.info("POST request: /api/inventory/release-stock for orderId: {}", orderId);
        InventoryService.ReleaseResponse response = inventoryService.releaseStock(orderId, requests);
        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory by product ID
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable String productId) {
        log.info("GET request: /api/inventory/{}", productId);
        InventoryDTO inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get all inventories
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventories() {
        log.info("GET request: /api/inventory");
        List<InventoryDTO> inventories = inventoryService.getAllInventories();
        return ResponseEntity.ok(inventories);
    }

    /**
     * Get low stock items
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockItems() {
        log.info("GET request: /api/inventory/low-stock");
        List<InventoryDTO> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    /**
     * Update inventory quantity
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable String productId,
            @RequestParam Integer quantityToAdd) {
        log.info("PUT request: /api/inventory/{} - Adding: {}", productId, quantityToAdd);
        InventoryDTO updatedInventory = inventoryService.updateInventory(productId, quantityToAdd);
        return ResponseEntity.ok(updatedInventory);
    }
}
