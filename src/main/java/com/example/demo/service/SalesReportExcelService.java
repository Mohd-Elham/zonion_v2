package com.example.demo.service;

import com.example.demo.models.Orders;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SalesReportExcelService {

    public byte[] generateSalesReportExcel(List<Orders> orders, long totalOrders, double totalSales,
                                           double couponDiscounts, double offerDiscounts, long productsSold,
                                           Map<String, Users> userMap, Map<String, Products> productMap) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));

            // Summary Section
            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Sales Report Summary");

            Row summaryRow1 = sheet.createRow(rowNum++);
            summaryRow1.createCell(0).setCellValue("Total Orders");
            summaryRow1.createCell(1).setCellValue(totalOrders);

            Row summaryRow2 = sheet.createRow(rowNum++);
            summaryRow2.createCell(0).setCellValue("Total Sales");
            summaryRow2.createCell(1).setCellValue(totalSales);
            summaryRow2.getCell(1).setCellStyle(currencyStyle);

            Row summaryRow3 = sheet.createRow(rowNum++);
            summaryRow3.createCell(0).setCellValue("Total Coupon Discounts");
            summaryRow3.createCell(1).setCellValue(couponDiscounts);

            Row summaryRow4 = sheet.createRow(rowNum++);
            summaryRow4.createCell(0).setCellValue("Total Offer Discounts");
            summaryRow4.createCell(1).setCellValue(offerDiscounts);
            summaryRow4.getCell(1).setCellStyle(currencyStyle);

            Row summaryRow5 = sheet.createRow(rowNum++);
            summaryRow5.createCell(0).setCellValue("Total no of Products Sold");
            summaryRow5.createCell(1).setCellValue(productsSold);
            summaryRow5.getCell(1).setCellStyle(currencyStyle);



            // Add empty row
            rowNum++;

            // Orders Table Header
            Row ordersHeader = sheet.createRow(rowNum++);
            String[] headers = {
                    "Order ID", "Date", "Customer", "Items",
                    "Amount", "Coupon Discount", "Offer Discount"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = ordersHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Orders Data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Orders order : orders) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getOrderDate().format(formatter));

                Users user = userMap.get(order.getUserId());
                row.createCell(2).setCellValue(user != null ? user.getUsername() : "N/A");

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
                row.createCell(3).setCellValue(items.toString());

                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(order.getTotalPrice());
                amountCell.setCellStyle(currencyStyle);

                Cell couponCell = row.createCell(5);
                couponCell.setCellValue(order.getCouponDiscount());
                couponCell.setCellStyle(currencyStyle);

                Cell offerCell = row.createCell(6);
                offerCell.setCellValue(order.getOfferDiscount());
                offerCell.setCellStyle(currencyStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

}
