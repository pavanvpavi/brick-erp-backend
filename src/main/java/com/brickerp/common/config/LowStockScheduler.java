package com.brickerp.common.config;

import com.brickerp.inventory.entity.Stock;
import com.brickerp.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockScheduler {

    private final StockRepository stockRepository;
    private final EmailService emailService;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void checkLowStock() {
        log.info("Running low stock check...");
        List<Stock> lowStockItems = stockRepository.findLowStockItems();

        if (lowStockItems.isEmpty()) {
            log.info("No low stock items found");
            return;
        }

        for (Stock stock : lowStockItems) {
            emailService.sendLowStockAlert(
                    stock.getProduct().getName(),
                    stock.getWarehouse().getName(),
                    stock.getQuantityOnHand(),
                    stock.getProduct().getMinimumStockLevel());
        }
        log.info("Low stock alerts sent for {} items", lowStockItems.size());
    }
}