package com.brickerp.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportResponse {
    private Long totalProducts;
    private Long lowStockCount;
    private Long outOfStockCount;
    private List<StockSummaryDto> stockSummary;
    private List<StockMovementSummaryDto> recentMovements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSummaryDto {
        private Long productId;
        private String productName;
        private String productSku;
        private String warehouseName;
        private Integer quantityOnHand;
        private Integer minimumStockLevel;
        private Boolean isLowStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockMovementSummaryDto {
        private String productName;
        private String movementType;
        private Integer quantity;
        private String warehouseName;
        private String createdAt;
    }
}