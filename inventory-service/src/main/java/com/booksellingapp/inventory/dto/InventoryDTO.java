package com.booksellingapp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {
    private Long id;
    private String productId;
    private String productName;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer totalQuantity;
    private Integer reorderLevel;
}
