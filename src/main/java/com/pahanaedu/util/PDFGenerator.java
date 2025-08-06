package com.pahanaedu.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.pahanaedu.model.Bill;
import com.pahanaedu.model.BillItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFGenerator {
    public static byte[] generateBillPDF(Bill bill) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Font setup
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Add title
        Paragraph title = new Paragraph("PAHANA EDU - INVOICE")
                .setFont(titleFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Add bill info
        Table infoTable = new Table(2);
        infoTable.setWidth(500);
        infoTable.setMarginBottom(20);

        addTableHeader(infoTable, "Bill Information", headerFont, 2);
        addTableRow(infoTable, "Bill No:", bill.getBillNo(), normalFont);
        addTableRow(infoTable, "Date:", bill.getCreatedDate().toString(), normalFont);

        document.add(infoTable);

        // Add customer info
        Table customerTable = new Table(2);
        customerTable.setWidth(500);
        customerTable.setMarginBottom(20);

        addTableHeader(customerTable, "Customer Information", headerFont, 2);
        // Add customer details here

        document.add(customerTable);

        // Add items table
        Table itemsTable = new Table(4);
        itemsTable.setWidth(500);
        itemsTable.setMarginBottom(20);

        // Add table headers
        addTableCell(itemsTable, "Item", headerFont);
        addTableCell(itemsTable, "Price", headerFont);
        addTableCell(itemsTable, "Quantity", headerFont);
        addTableCell(itemsTable, "Subtotal", headerFont);

        // Add items
        if (bill.getItems() != null) {
            for (BillItem item : bill.getItems()) {
                String itemName = (item.getItem() != null) ? item.getItem().getName() : "Item " + item.getItemId();
                addTableCell(itemsTable, itemName, normalFont);
                addTableCell(itemsTable, String.format("LKR %.2f", item.getPrice()), normalFont);
                addTableCell(itemsTable, String.valueOf(item.getQuantity()), normalFont);
                addTableCell(itemsTable, String.format("LKR %.2f", item.getSubtotal()), normalFont);
            }
        }

        document.add(itemsTable);

        // Add total
        Paragraph total = new Paragraph(
                String.format("Total: LKR %.2f", bill.getTotalAmount()))
                .setFont(headerFont)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(total);

        document.close();
        return baos.toByteArray();
    }

    private static void addTableHeader(Table table, String text, PdfFont font, int colspan) {
        Cell cell = new Cell(1, colspan)
                .add(new Paragraph(text).setFont(font))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(211, 211, 211)); // LIGHT_GRAY
        table.addCell(cell);
    }

    private static void addTableRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Paragraph(label).setFont(font));
        table.addCell(new Paragraph(value).setFont(font));
    }

    private static void addTableCell(Table table, String text, PdfFont font) {
        table.addCell(new Paragraph(text).setFont(font));
    }
}