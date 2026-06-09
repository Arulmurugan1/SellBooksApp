package com.booksellingapp.inventory.service;

import com.booksellingapp.inventory.dto.InventoryDTO;
import com.booksellingapp.inventory.entity.Inventory;
import com.booksellingapp.inventory.entity.InventoryReservation;
import com.booksellingapp.inventory.repository.InventoryRepository;
import com.booksellingapp.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    /**
     * Check if items are in stock
     */
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(List<StockCheckRequest> requests) {
        log.info("Checking stock for {} items", requests.size());

        List<StockCheckDetail> details = requests.stream()
                .map(request -> {
                    Inventory inventory = inventoryRepository.findByProductId(request.productId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

                    boolean available = inventory.getQuantityAvailable() >= request.quantity();
                    log.debug("Product {} - Available: {}, Requested: {}, Current: {}",
                            request.productId(), available, request.quantity(), inventory.getQuantityAvailable());

                    return new StockCheckDetail(
                            request.productId(),
                            available,
                            inventory.getQuantityAvailable()
                    );
                })
                .collect(Collectors.toList());

        boolean allItemsAvailable = details.stream().allMatch(StockCheckDetail::available);
        return new StockCheckResponse(allItemsAvailable, details);
    }

    /**
     * Reserve stock for an order
     */
    @Transactional
    public ReservationResponse reserveStock(String orderId, List<ReservationRequest> requests) {
        log.info("Reserving stock for orderId: {}", orderId);

        List<ReservationDetail> details = new java.util.ArrayList<>();

        for (ReservationRequest request : requests) {
            Inventory inventory = inventoryRepository.findByProductId(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

            if (inventory.getQuantityAvailable() < request.quantity()) {
                throw new RuntimeException("Insufficient stock for product: " + request.productId());
            }

            // Decrease available quantity
            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - request.quantity());
            // Increase reserved quantity
            inventory.setQuantityReserved(inventory.getQuantityReserved() + request.quantity());
            Inventory saved = inventoryRepository.save(inventory);

            // Create reservation record
            InventoryReservation reservation = InventoryReservation.builder()
                    .orderId(orderId)
                    .productId(request.productId())
                    .quantity(request.quantity())
                    .status(InventoryReservation.ReservationStatus.RESERVED)
                    .build();
            reservationRepository.save(reservation);

            details.add(new ReservationDetail(
                    request.productId(),
                    request.quantity(),
                    saved.getQuantityAvailable(),
                    saved.getQuantityReserved()
            ));

            log.debug("Stock reserved for orderId: {}, productId: {}, quantity: {}",
                    orderId, request.productId(), request.quantity());
        }

        log.info("Stock reservation completed for orderId: {}", orderId);
        return new ReservationResponse(orderId, "RESERVED", details);
    }

    /**
     * Release reserved stock (compensation transaction in saga)
     */
    @Transactional
    public ReleaseResponse releaseStock(String orderId, List<ReleaseRequest> requests) {
        log.info("Releasing reserved stock for orderId: {}", orderId);

        List<ReleaseDetail> details = new java.util.ArrayList<>();

        for (ReleaseRequest request : requests) {
            Inventory inventory = inventoryRepository.findByProductId(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

            // Increase available quantity
            inventory.setQuantityAvailable(inventory.getQuantityAvailable() + request.quantity());
            // Decrease reserved quantity
            inventory.setQuantityReserved(inventory.getQuantityReserved() - request.quantity());
            Inventory saved = inventoryRepository.save(inventory);

            // Update reservation record
            List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
            for (InventoryReservation reservation : reservations) {
                if (reservation.getProductId().equals(request.productId())) {
                    reservation.setStatus(InventoryReservation.ReservationStatus.RELEASED);
                    reservationRepository.save(reservation);
                }
            }

            details.add(new ReleaseDetail(
                    request.productId(),
                    request.quantity(),
                    saved.getQuantityAvailable()
            ));

            log.debug("Stock released for orderId: {}, productId: {}, quantity: {}",
                    orderId, request.productId(), request.quantity());
        }

        log.info("Stock release completed for orderId: {}", orderId);
        return new ReleaseResponse(orderId, "RELEASED", details);
    }

    /**
     * Get inventory by product ID
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(String productId) {
        log.info("Fetching inventory for productId: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
    }

    /**
     * Get all inventories
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllInventories() {
        log.info("Fetching all inventories");
        return inventoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStockItems() {
        log.info("Fetching low stock items");
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.getQuantityAvailable() <= inv.getReorderLevel())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update inventory
     */
    @Transactional
    public InventoryDTO updateInventory(String productId, Integer quantityToAdd) {
        log.info("Updating inventory for productId: {} - Adding: {}", productId, quantityToAdd);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantityToAdd);
        inventory.setLastRestockDate(LocalDateTime.now());

        log.debug("Inventory before update for productId: {} - Available: {}, Reserved: {}",
                productId, inventory.getQuantityAvailable(), inventory.getQuantityReserved());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Inventory updated for productId: {}", productId);

        log.debug("Inventory after update for productId: {} - Available: {}, Reserved: {}",
                productId, updatedInventory.getQuantityAvailable(), updatedInventory.getQuantityReserved());

        return convertToDTO(updatedInventory);
    }

    /**
     * Convert Inventory entity to DTO
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(inventory.getProductName())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .totalQuantity(inventory.getTotalQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .build();
    }

    // DTOs for requests and responses
    public record StockCheckRequest(String productId, Integer quantity) {}
    public record StockCheckResponse(boolean allItemsAvailable, List<StockCheckDetail> details) {}
    public record StockCheckDetail(String productId, boolean available, Integer availableQuantity) {}
    public record ReservationRequest(String productId, Integer quantity) {}
    public record ReleaseRequest(String productId, Integer quantity) {}

    public record ReservationDetail(String productId, Integer quantity, Integer quantityAvailable, Integer quantityReserved) {}
    public record ReservationResponse(String orderId, String status, List<ReservationDetail> details) {}

    public record ReleaseDetail(String productId, Integer quantity, Integer quantityAvailable) {}
    public record ReleaseResponse(String orderId, String status, List<ReleaseDetail> details) {}
}
