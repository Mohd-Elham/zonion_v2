package com.example.demo.service;

import com.example.demo.models.Orders;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SalesReportPdfService {

    public byte[] generateSalesReportPdf(List<Orders> orders, long totalOrders, double totalSales,
                                         double couponDiscounts, double offerDiscounts, long productsSold,
                                         Map<String, Users> userMap, Map<String, Products> productMap) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Summary Table
        PdfPTable summaryTable = new PdfPTable(5);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(10);

        addSummaryCell(summaryTable, "Total Orders", String.valueOf(totalOrders));
        addSummaryCell(summaryTable, "Total Sales", "₹" + String.format("%.2f", totalSales));
        addSummaryCell(summaryTable, "Coupon Discounts", "-₹" + String.format("%.2f", couponDiscounts));
        addSummaryCell(summaryTable, "Offer Discounts", "-₹" + String.format("%.2f", offerDiscounts));
        addSummaryCell(summaryTable, "Products Sold", String.valueOf(productsSold));

        document.add(summaryTable);

        // Orders Table
        PdfPTable ordersTable = new PdfPTable(7);
        ordersTable.setWidthPercentage(100);
        ordersTable.setSpacingBefore(10);

        // Table Headers
        addTableHeader(ordersTable, "Order ID");
        addTableHeader(ordersTable, "Date");
        addTableHeader(ordersTable, "Customer");
        addTableHeader(ordersTable, "Items");
        addTableHeader(ordersTable, "Amount");
        addTableHeader(ordersTable, "Coupon Discount");
        addTableHeader(ordersTable, "Offer Discount");

        // Table Rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Orders order : orders) {
            ordersTable.addCell(order.getId());
            ordersTable.addCell(order.getOrderDate().format(formatter));
            ordersTable.addCell(userMap.getOrDefault(order.getUserId(), new Users()).getUsername());

            StringBuilder items = new StringBuilder();
            for (Orders.OrderItem item : order.getItems()) {
                Products product = productMap.get(item.getProductId());
                if (product != null) {
                    items.append(product.getProduct_name())
                            .append(" ×")
                            .append(item.getQuantity())
                            .append("\n");
                }
            }
            ordersTable.addCell(items.toString());

            ordersTable.addCell("₹" + String.format("%.2f", order.getTotalPrice()));
            ordersTable.addCell("-₹" + String.format("%.2f", order.getCouponDiscount()));
            ordersTable.addCell("-₹" + String.format("%.2f", order.getOfferDiscount()));
        }

        document.add(ordersTable);
        document.close();

        return outputStream.toByteArray();
    }

    private void addSummaryCell(PdfPTable table, String title, String value) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 14);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderWidth(1);

        Paragraph p = new Paragraph();
        p.add(new Chunk(title + "\n", headerFont));
        p.add(new Chunk(value, valueFont));

        cell.addElement(p);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }
}