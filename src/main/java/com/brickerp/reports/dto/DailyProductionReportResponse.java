package com.brickerp.reports.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DailyProductionReportResponse {
    private String period;
    private Long totalProductionOrders;
    private Long completedOrders;
    private Long inProgressOrders;
    private Long plannedOrders;
    private Long cancelledOrders;
    private Integer totalPlannedQuantity;
    private Integer totalProducedQuantity;
    private Double completionRate;
    private List<ProductionEntry> productionOrders;
    private List<ProductSummary> productSummary;

    @Data
    @Builder
    public static class ProductionEntry {
        private String productionNumber;
        private String productName;
        private String status;
        private Integer plannedQuantity;
        private Integer producedQuantity;
        private String plannedStartDate;
        private String actualStartDate;
        private String actualEndDate;
        private String warehouseName;
    }

    @Data
    @Builder
    public static class ProductSummary {
        private String productName;
        private Integer totalProduced;
        private Long orderCount;
    }
}