package com.brickerp.common.pdf;

import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.dispatch.entity.DeliveryOrder;
import com.brickerp.dispatch.repository.DeliveryOrderRepository;
import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.repository.InvoiceRepository;
import com.brickerp.inventory.entity.Warehouse;
import com.brickerp.inventory.repository.WarehouseRepository;
import com.brickerp.procurement.entity.PurchaseOrder;
import com.brickerp.procurement.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
@Slf4j
public class PdfController {

    private final PdfService pdfService;
    private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final WarehouseRepository warehouseRepository;

    @GetMapping("/invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

            byte[] pdf = pdfService.generateInvoicePdf(invoice);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"Invoice_"
                                    + invoice.getInvoiceNumber() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    @GetMapping("/purchase-order/{id}")
    public ResponseEntity<byte[]> downloadPurchaseOrder(@PathVariable Long id) {
        try {
            PurchaseOrder po = purchaseOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

            String warehouseName = "—";
            if (po.getWarehouseId() != null) {
                Optional<Warehouse> wh = warehouseRepository.findById(po.getWarehouseId());
                warehouseName = wh.map(Warehouse::getName).orElse("—");
            }

            byte[] pdf = pdfService.generatePurchaseOrderPdf(po, warehouseName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"PO_"
                                    + po.getPoNumber() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate PO PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    @GetMapping("/delivery/{id}")
    public ResponseEntity<byte[]> downloadDeliveryChallan(@PathVariable Long id) {
        try {
            DeliveryOrder delivery = deliveryOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));

            byte[] pdf = pdfService.generateDeliveryChallanPdf(delivery);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"Challan_"
                                    + delivery.getDeliveryNumber() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(pdf);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate delivery challan PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }
}