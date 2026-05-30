package com.brickerp.common.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.brickerp.finance.entity.Invoice;
import com.brickerp.finance.entity.InvoiceItem;
import com.brickerp.procurement.entity.PurchaseOrder;
import com.brickerp.procurement.entity.PurchaseOrderItem;
import com.brickerp.dispatch.entity.DeliveryOrder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfService {

    // Colors
    private static final BaseColor PRIMARY = new BaseColor(180, 83, 9);
    private static final BaseColor LIGHT_GRAY = new BaseColor(243, 244, 246);
    private static final BaseColor DARK_GRAY = new BaseColor(55, 65, 81);
    private static final BaseColor WHITE = BaseColor.WHITE;
    private static final BaseColor GREEN = new BaseColor(22, 163, 74);
    private static final BaseColor RED = new BaseColor(220, 38, 38);

    // Fonts
    private Font titleFont() {
        return new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, WHITE);
    }

    private Font headerFont() {
        return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, DARK_GRAY);
    }

    private Font normalFont() {
        return new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, DARK_GRAY);
    }

    private Font boldFont() {
        return new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, DARK_GRAY);
    }

    private Font tableHeaderFont() {
        return new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, WHITE);
    }

    private Font smallFont() {
        return new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, DARK_GRAY);
    }

    private Font amountFont() {
        return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, PRIMARY);
    }

    // ==================== INVOICE PDF ====================

    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Header
        addCompanyHeader(doc, "TAX INVOICE", invoice.getInvoiceNumber());

        // Invoice Info + Customer Info
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 1, 1 });
        infoTable.setSpacingBefore(15);

        // Invoice details
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(LIGHT_GRAY);
        leftCell.setPadding(10);
        leftCell.addElement(new Paragraph("INVOICE DETAILS", headerFont()));
        leftCell.addElement(new Paragraph(" ", smallFont()));
        leftCell.addElement(new Paragraph("Invoice No: " + invoice.getInvoiceNumber(), boldFont()));
        leftCell.addElement(new Paragraph("Date: " + invoice.getInvoiceDate(), normalFont()));
        leftCell.addElement(new Paragraph("Due Date: " +
                (invoice.getDueDate() != null ? invoice.getDueDate() : "—"), normalFont()));
        leftCell.addElement(new Paragraph("Status: " + invoice.getStatus().name(), boldFont()));
        if (invoice.getSalesOrder() != null) {
            leftCell.addElement(new Paragraph("Order Ref: " +
                    invoice.getSalesOrder().getOrderNumber(), normalFont()));
        }
        infoTable.addCell(leftCell);

        // Customer details
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(LIGHT_GRAY);
        rightCell.setPadding(10);
        rightCell.addElement(new Paragraph("BILL TO", headerFont()));
        rightCell.addElement(new Paragraph(" ", smallFont()));
        rightCell.addElement(new Paragraph(invoice.getCustomer().getName(), boldFont()));
        rightCell.addElement(new Paragraph("Code: " +
                invoice.getCustomer().getCustomerCode(), normalFont()));
        if (invoice.getCustomer().getPhone() != null) {
            rightCell.addElement(new Paragraph("Phone: " +
                    invoice.getCustomer().getPhone(), normalFont()));
        }
        if (invoice.getCustomer().getEmail() != null) {
            rightCell.addElement(new Paragraph("Email: " +
                    invoice.getCustomer().getEmail(), normalFont()));
        }
        if (invoice.getCustomer().getGstin() != null) {
            rightCell.addElement(new Paragraph("GSTIN: " +
                    invoice.getCustomer().getGstin(), normalFont()));
        }
        infoTable.addCell(rightCell);
        doc.add(infoTable);

        // Items Table
        doc.add(new Paragraph(" "));
        PdfPTable itemsTable = new PdfPTable(6);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[] { 0.5f, 3f, 1f, 1.2f, 1.2f, 1.5f });

        addTableHeader(itemsTable,
                new String[] { "#", "Product", "Qty", "Unit Price", "Discount", "Total" });

        int sno = 1;
        for (InvoiceItem item : invoice.getItems()) {
            addTableRow(itemsTable, new String[] {
                    String.valueOf(sno++),
                    item.getProductName(),
                    String.valueOf(item.getQuantity()),
                    "₹" + item.getUnitPrice().toPlainString(),
                    "₹" + item.getDiscountAmount().toPlainString(),
                    "₹" + item.getLineTotal().toPlainString()
            }, sno % 2 == 0 ? LIGHT_GRAY : WHITE);
        }
        doc.add(itemsTable);

        // Totals
        addInvoiceTotals(doc, invoice);

        // Payment History
        if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
            doc.add(new Paragraph(" "));
            Paragraph payTitle = new Paragraph("PAYMENT HISTORY", headerFont());
            payTitle.setSpacingBefore(10);
            doc.add(payTitle);

            PdfPTable payTable = new PdfPTable(4);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new float[] { 1.5f, 2f, 2f, 2f });
            addTableHeader(payTable,
                    new String[] { "Payment #", "Date", "Method", "Amount" });

            invoice.getPayments().forEach(p -> {
                addTableRow(payTable, new String[] {
                        p.getPaymentNumber(),
                        p.getPaymentDate().toString(),
                        p.getPaymentMethod().name(),
                        "₹" + p.getAmount().toPlainString()
                }, WHITE);
            });
            doc.add(payTable);
        }

        // Footer
        addFooter(doc);
        doc.close();
        return out.toByteArray();
    }

    // ==================== PURCHASE ORDER PDF ====================

    public byte[] generatePurchaseOrderPdf(PurchaseOrder po) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addCompanyHeader(doc, "PURCHASE ORDER", po.getPoNumber());

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 1, 1 });
        infoTable.setSpacingBefore(15);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(LIGHT_GRAY);
        leftCell.setPadding(10);
        leftCell.addElement(new Paragraph("PO DETAILS", headerFont()));
        leftCell.addElement(new Paragraph(" ", smallFont()));
        leftCell.addElement(new Paragraph("PO Number: " + po.getPoNumber(), boldFont()));
        leftCell.addElement(new Paragraph("Order Date: " + po.getOrderDate(), normalFont()));
        leftCell.addElement(new Paragraph("Expected Delivery: " +
                (po.getExpectedDeliveryDate() != null
                        ? po.getExpectedDeliveryDate()
                        : "—"),
                normalFont()));
        leftCell.addElement(new Paragraph("Status: " + po.getStatus().name(), boldFont()));
        leftCell.addElement(new Paragraph("Warehouse: " +
                po.getWarehouse().getName(), normalFont()));
        infoTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(LIGHT_GRAY);
        rightCell.setPadding(10);
        rightCell.addElement(new Paragraph("SUPPLIER", headerFont()));
        rightCell.addElement(new Paragraph(" ", smallFont()));
        rightCell.addElement(new Paragraph(po.getSupplier().getName(), boldFont()));
        rightCell.addElement(new Paragraph("Code: " +
                po.getSupplier().getSupplierCode(), normalFont()));
        if (po.getSupplier().getPhone() != null) {
            rightCell.addElement(new Paragraph("Phone: " +
                    po.getSupplier().getPhone(), normalFont()));
        }
        if (po.getSupplier().getContactPerson() != null) {
            rightCell.addElement(new Paragraph("Contact: " +
                    po.getSupplier().getContactPerson(), normalFont()));
        }
        if (po.getSupplier().getGstin() != null) {
            rightCell.addElement(new Paragraph("GSTIN: " +
                    po.getSupplier().getGstin(), normalFont()));
        }
        infoTable.addCell(rightCell);
        doc.add(infoTable);

        doc.add(new Paragraph(" "));
        PdfPTable itemsTable = new PdfPTable(6);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[] { 0.5f, 3f, 1f, 1.5f, 1f, 1.5f });

        addTableHeader(itemsTable,
                new String[] { "#", "Product", "Ordered", "Unit Price", "Tax%", "Total" });

        int sno = 1;
        for (PurchaseOrderItem item : po.getItems()) {
            addTableRow(itemsTable, new String[] {
                    String.valueOf(sno++),
                    item.getProduct().getName(),
                    String.valueOf(item.getQuantityOrdered()),
                    "₹" + item.getUnitPrice().toPlainString(),
                    item.getTaxPercentage() + "%",
                    "₹" + item.getLineTotal().toPlainString()
            }, sno % 2 == 0 ? LIGHT_GRAY : WHITE);
        }
        doc.add(itemsTable);

        // PO Totals
        addPoTotals(doc, po);

        if (po.getNotes() != null && !po.getNotes().isEmpty()) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Notes: " + po.getNotes(), normalFont()));
        }

        addFooter(doc);
        doc.close();
        return out.toByteArray();
    }

    // ==================== DELIVERY CHALLAN PDF ====================

    public byte[] generateDeliveryChallanPdf(DeliveryOrder delivery) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addCompanyHeader(doc, "DELIVERY CHALLAN", delivery.getDeliveryNumber());

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 1, 1 });
        infoTable.setSpacingBefore(15);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(LIGHT_GRAY);
        leftCell.setPadding(10);
        leftCell.addElement(new Paragraph("DELIVERY DETAILS", headerFont()));
        leftCell.addElement(new Paragraph(" ", smallFont()));
        leftCell.addElement(new Paragraph("Challan No: " +
                delivery.getDeliveryNumber(), boldFont()));
        leftCell.addElement(new Paragraph("Delivery Date: " +
                (delivery.getDeliveryDate() != null
                        ? delivery.getDeliveryDate()
                        : LocalDate.now()),
                normalFont()));
        leftCell.addElement(new Paragraph("Status: " +
                delivery.getStatus().name(), boldFont()));
        leftCell.addElement(new Paragraph("Order Ref: " +
                delivery.getSalesOrder().getOrderNumber(), normalFont()));
        if (delivery.getVehicleNumber() != null) {
            leftCell.addElement(new Paragraph("Vehicle: " +
                    delivery.getVehicleNumber(), normalFont()));
        }
        if (delivery.getDriverName() != null) {
            leftCell.addElement(new Paragraph("Driver: " +
                    delivery.getDriverName(), normalFont()));
        }
        infoTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(LIGHT_GRAY);
        rightCell.setPadding(10);
        rightCell.addElement(new Paragraph("DELIVER TO", headerFont()));
        rightCell.addElement(new Paragraph(" ", smallFont()));
        rightCell.addElement(new Paragraph(
                delivery.getSalesOrder().getCustomer().getName(), boldFont()));
        rightCell.addElement(new Paragraph("Code: " +
                delivery.getSalesOrder().getCustomer().getCustomerCode(), normalFont()));
        if (delivery.getSalesOrder().getCustomer().getPhone() != null) {
            rightCell.addElement(new Paragraph("Phone: " +
                    delivery.getSalesOrder().getCustomer().getPhone(), normalFont()));
        }
        if (delivery.getDeliveryAddress() != null
                && !delivery.getDeliveryAddress().isEmpty()) {
            rightCell.addElement(new Paragraph("Address: " +
                    delivery.getDeliveryAddress(), normalFont()));
        }
        if (delivery.getReceivedBy() != null) {
            rightCell.addElement(new Paragraph("Received By: " +
                    delivery.getReceivedBy(), normalFont()));
        }
        infoTable.addCell(rightCell);
        doc.add(infoTable);

        // Order Items
        doc.add(new Paragraph(" "));
        Paragraph itemsTitle = new Paragraph("ITEMS DELIVERED", headerFont());
        itemsTitle.setSpacingBefore(5);
        doc.add(itemsTitle);
        doc.add(new Paragraph(" "));

        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[] { 0.5f, 4f, 1.5f, 1.5f });
        addTableHeader(itemsTable,
                new String[] { "#", "Product", "Quantity", "Unit" });

        int sno = 1;
        for (var item : delivery.getSalesOrder().getItems()) {
            addTableRow(itemsTable, new String[] {
                    String.valueOf(sno++),
                    item.getProduct().getName(),
                    String.valueOf(item.getQuantity()),
                    item.getProduct().getUom() != null
                            ? item.getProduct().getUom().getAbbreviation()
                            : "PCS"
            }, sno % 2 == 0 ? LIGHT_GRAY : WHITE);
        }
        doc.add(itemsTable);

        // Signature section
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(30);

        PdfPCell dispatchSign = new PdfPCell();
        dispatchSign.setBorder(Rectangle.TOP);
        dispatchSign.setPaddingTop(10);
        dispatchSign.addElement(new Paragraph(
                "Dispatched By", normalFont()));
        signTable.addCell(dispatchSign);

        PdfPCell receiveSign = new PdfPCell();
        receiveSign.setBorder(Rectangle.TOP);
        receiveSign.setPaddingTop(10);
        receiveSign.addElement(new Paragraph(
                "Received By", normalFont()));
        signTable.addCell(receiveSign);
        doc.add(signTable);

        addFooter(doc);
        doc.close();
        return out.toByteArray();
    }

    // ==================== HELPER METHODS ====================

    private void addCompanyHeader(Document doc, String docType,
            String docNumber) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 2, 1 });

        // Company info
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBackgroundColor(PRIMARY);
        companyCell.setPadding(15);
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.addElement(new Paragraph("BRICK ERP", titleFont()));
        companyCell.addElement(new Paragraph(
                "Brick Manufacturing & Trading", normalFont()));
        companyCell.addElement(new Paragraph(
                "Karnataka, India", normalFont()));
        header.addCell(companyCell);

        // Document type
        PdfPCell docCell = new PdfPCell();
        docCell.setBackgroundColor(DARK_GRAY);
        docCell.setPadding(15);
        docCell.setBorder(Rectangle.NO_BORDER);
        docCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font docTypeFont = new Font(
                Font.FontFamily.HELVETICA, 14, Font.BOLD, WHITE);
        Font docNumFont = new Font(
                Font.FontFamily.HELVETICA, 11, Font.NORMAL, WHITE);
        Paragraph docTypePara = new Paragraph(docType, docTypeFont);
        docTypePara.setAlignment(Element.ALIGN_RIGHT);
        docCell.addElement(docTypePara);
        Paragraph docNumPara = new Paragraph(docNumber, docNumFont);
        docNumPara.setAlignment(Element.ALIGN_RIGHT);
        docCell.addElement(docNumPara);
        Paragraph datePara = new Paragraph(
                LocalDate.now().toString(),
                new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, WHITE));
        datePara.setAlignment(Element.ALIGN_RIGHT);
        docCell.addElement(datePara);
        header.addCell(docCell);

        doc.add(header);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont()));
            cell.setBackgroundColor(PRIMARY);
            cell.setPadding(8);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String[] values, BaseColor bg) {
        for (int i = 0; i < values.length; i++) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(values[i] != null ? values[i] : "—", normalFont()));
            cell.setBackgroundColor(bg);
            cell.setPadding(7);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(
                    i == 0 || i == 1 ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addInvoiceTotals(Document doc, Invoice invoice) throws Exception {
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(50);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingBefore(10);
        totals.setWidths(new float[] { 2, 1.5f });

        addTotalRow(totals, "Subtotal:", "₹" + invoice.getSubtotal().toPlainString());
        addTotalRow(totals, "Discount:", "₹" + invoice.getDiscountAmount().toPlainString());
        addTotalRow(totals, "Tax Amount:", "₹" + invoice.getTaxAmount().toPlainString());

        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL:", amountFont()));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);
        totals.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(
                new Phrase("₹" + invoice.getTotalAmount().toPlainString(), amountFont()));
        valueCell.setBackgroundColor(LIGHT_GRAY);
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(valueCell);

        addTotalRow(totals, "Paid Amount:", "₹" + invoice.getPaidAmount().toPlainString());
        addTotalRow(totals, "Balance Due:", "₹" + invoice.getBalanceDue().toPlainString());
        doc.add(totals);
    }

    private void addPoTotals(Document doc, PurchaseOrder po) throws Exception {
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(50);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingBefore(10);
        totals.setWidths(new float[] { 2, 1.5f });

        addTotalRow(totals, "Subtotal:", "₹" + po.getSubtotal().toPlainString());
        addTotalRow(totals, "Tax Amount:", "₹" + po.getTaxAmount().toPlainString());

        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL:", amountFont()));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);
        totals.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(
                new Phrase("₹" + po.getTotalAmount().toPlainString(), amountFont()));
        valueCell.setBackgroundColor(LIGHT_GRAY);
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(valueCell);

        doc.add(totals);
    }

    private void addTotalRow(PdfPTable table,
            String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, normalFont()));
        labelCell.setPadding(6);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont()));
        valueCell.setPadding(6);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addFooter(Document doc) throws Exception {
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
                "Generated by Brick ERP System | " + LocalDate.now(),
                new Font(Font.FontFamily.HELVETICA, 8,
                        Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }
}