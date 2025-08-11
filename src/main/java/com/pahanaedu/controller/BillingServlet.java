package com.pahanaedu.controller;

import com.pahanaedu.dao.BillDAO;
import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.*;
import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@WebServlet("/billing")
public class BillingServlet extends HttpServlet {
    private CustomerDAO customerDAO;
    private ItemDAO itemDAO;
    private BillDAO billDAO;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        customerDAO = new CustomerDAO();
        itemDAO = new ItemDAO();
        try {
            billDAO = new BillDAO();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "checkCustomer":
                    checkCustomer(request, response);
                    break;
                case "downloadBill":
                    downloadBill(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid action");
                    response.getWriter().write(gson.toJson(errorResponse));
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Database error occurred: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            ex.printStackTrace();
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Server error occurred: " + ex.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
            ex.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "checkCustomer":
                    checkCustomer(request, response);
                    break;
                case "selectCustomer":
                    selectCustomer(request, response);
                    break;
                case "clearCustomer":
                    clearCustomer(request, response);
                    break;
                case "addCustomer":
                    addCustomer(request, response);
                    break;
                case "generateBill":
                    generateBill(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void checkCustomer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Phone number is required\"}");
            return;
        }

        try {
            List<Customer> customers = customerDAO.getCustomersByPhone(phone);

            Map<String, Object> responseData = new HashMap<>();
            if (customers.isEmpty()) {
                responseData.put("error", "No customers found with this phone number");
            } else {
                responseData.put("customers", customers);
            }

            response.getWriter().write(new Gson().toJson(responseData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
            e.printStackTrace();
        }
    }

    private void selectCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Customer customer = gson.fromJson(request.getReader(), Customer.class);
        request.getSession().setAttribute("selectedCustomer", customer);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void clearCustomer(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("selectedCustomer");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void addCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parse the JSON request
            Customer customer = gson.fromJson(request.getReader(), Customer.class);

            // Generate account number
            customer.setAccountNo(customerDAO.generateNewAccountNumber());

            // Set created by (from session)
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            customer.setCreatedBy(username != null ? username : "system");

            // Set default values
            customer.setUnitsConsumed(0);

            // Validate required fields
            if (customer.getName() == null || customer.getName().trim().isEmpty() ||
                    customer.getPhoneNo() == null || customer.getPhoneNo().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Name and phone number are required\"}");
                return;
            }

            // Check if phone number already exists
            if (customerDAO.phoneNoExists(customer.getPhoneNo())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"Phone number already exists\"}");
                return;
            }

            // Insert customer
            boolean success = customerDAO.insertCustomer(customer);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(customer));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to save customer to database\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private Map<String, String> validateBill(Bill bill) throws SQLException {
        Map<String, String> errors = new HashMap<>();

        // 1. Validate customer
        if (bill.getCustomerId() == null || bill.getCustomerId().trim().isEmpty()) {
            errors.put("customer", "Customer not selected");
        } else if (customerDAO.getCustomerByAccountNo(bill.getCustomerId()) == null) {
            errors.put("customer", "Invalid customer selected");
        }

        // 2. Validate items
        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            errors.put("items", "No items in the bill");
        } else {
            for (BillItem item : bill.getItems()) {
                Item dbItem = itemDAO.getItemById(item.getItemId());
                if (dbItem == null) {
                    errors.put("items", "Invalid item in cart: " + item.getItemId());
                    break;
                }
                if (item.getQuantity() <= 0) {
                    errors.put("items", "Invalid quantity for item: " + item.getItemId());
                    break;
                }
                if (dbItem.getStockQty() < item.getQuantity()) {
                    errors.put("items", "Insufficient stock for item: " + dbItem.getName());
                    break;
                }
            }
        }

        // 3. Validate payment
        if (!"cash".equals(bill.getPaymentMethod()) && !"card".equals(bill.getPaymentMethod())) {
            errors.put("payment", "Invalid payment method");
        }
        if (bill.getTotalAmount() <= 0) {
            errors.put("total", "Invalid total amount");
        }

        return errors;
    }

    private void generateBill(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Bill bill = gson.fromJson(request.getReader(), Bill.class);
            HttpSession session = request.getSession();

            // Get user ID from session (not username)
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }

            // Set createdBy with the numeric user ID
            bill.setCreatedBy(userId);

            // Rest of your validation and bill creation logic...
            Map<String, String> errors = validateBill(bill);

            if (!errors.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(errors));
                return;
            }

            boolean success = billDAO.createBill(bill);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Bill created successfully");
                responseData.put("billNo", bill.getBillNo());
                response.getWriter().write(gson.toJson(responseData));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to create bill\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void downloadBill(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        String billNo = request.getParameter("billNo");

        if (billNo == null || billNo.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bill number is required");
            return;
        }

        try {
            // Get the bill from database
            Bill bill = billDAO.getBillByNumber(billNo);

            if (bill == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Bill not found");
                return;
            }

            // Generate PDF
            byte[] pdfBytes;
            try {
                pdfBytes = generateBillPdf(bill);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error generating PDF: " + e.getMessage());
                return;
            }

            // Set response headers for PDF download
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=bill_" + billNo + ".pdf");
            response.setContentLength(pdfBytes.length);

            // Write PDF to response
            try (OutputStream out = response.getOutputStream()) {
                out.write(pdfBytes);
                out.flush();
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error processing bill download: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] generateBillPdf(Bill bill) throws SQLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Get customer details
            Customer customer = customerDAO.getCustomerByAccountNo(bill.getCustomerId());

            // Add company header
            Paragraph companyHeader = new Paragraph("PAHANA EDU",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD));
            companyHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(companyHeader);

            Paragraph companySub = new Paragraph("Education Management System",
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            companySub.setAlignment(Element.ALIGN_CENTER);
            document.add(companySub);

            document.add(new Paragraph(" "));

            // Add invoice title
            Paragraph invoiceTitle = new Paragraph("INVOICE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD));
            invoiceTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(invoiceTitle);

            document.add(new Paragraph(" "));

            // Add bill info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            // Left column - Bill info
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("Bill No: " + bill.getBillNo()));
            leftCell.addElement(new Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(bill.getCreatedDate())));
            infoTable.addCell(leftCell);

            // Right column - Customer info
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            if (customer != null) {
                rightCell.addElement(new Paragraph("Customer: " + customer.getName()));
                rightCell.addElement(new Paragraph("Account No: " + customer.getAccountNo()));
                rightCell.addElement(new Paragraph("Phone: " + customer.getPhoneNo()));
            }
            infoTable.addCell(rightCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Add items table
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3, 2, 2, 2, 3});

            // Table headers
            itemsTable.addCell(createHeaderCell("Description"));
            itemsTable.addCell(createHeaderCell("Unit Price"));
            itemsTable.addCell(createHeaderCell("Qty"));
            itemsTable.addCell(createHeaderCell("Amount"));
            itemsTable.addCell(createHeaderCell("Notes"));

            // Add items
            for (BillItem item : bill.getItems()) {
                Item dbItem = itemDAO.getItemById(item.getItemId());
                if (dbItem != null) {
                    itemsTable.addCell(createContentCell(dbItem.getName()));
                    itemsTable.addCell(createContentCell("LKR " + String.format("%.2f", item.getPrice())));
                    itemsTable.addCell(createContentCell(String.valueOf(item.getQuantity())));
                    itemsTable.addCell(createContentCell("LKR " + String.format("%.2f", item.getSubtotal())));
                    itemsTable.addCell(createContentCell(dbItem.getDescription() != null ? dbItem.getDescription() : ""));
                }
            }

            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // Add totals section
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{2, 1});

            // Calculate tax (8%)
            double taxAmount = bill.getTotalAmount() * 0.08;
            double totalWithTax = bill.getTotalAmount() + taxAmount;

            totalsTable.addCell(createTotalLabelCell("Subtotal:"));
            totalsTable.addCell(createTotalValueCell("LKR " + String.format("%.2f", bill.getTotalAmount())));

            totalsTable.addCell(createTotalLabelCell("Tax (8%):"));
            totalsTable.addCell(createTotalValueCell("LKR " + String.format("%.2f", taxAmount)));

            totalsTable.addCell(createTotalLabelCell("Total:"));
            totalsTable.addCell(createTotalValueCell("LKR " + String.format("%.2f", totalWithTax)));

            document.add(totalsTable);
            document.add(new Paragraph(" "));

            // Payment information
            Paragraph paymentInfo = new Paragraph("Payment Information",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD));
            document.add(paymentInfo);

            document.add(new Paragraph("Method: " + bill.getPaymentMethod().toUpperCase()));
            document.add(new Paragraph("Details: " + bill.getPaymentDetails()));
            document.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph("Thank you for your business!",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            Paragraph terms = new Paragraph("Terms & Conditions: Goods sold are not returnable.",
                    FontFactory.getFont(FontFactory.HELVETICA, 8));
            terms.setAlignment(Element.ALIGN_CENTER);
            document.add(terms);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF document", e);
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }

        return baos.toByteArray();
    }

    // Helper methods for creating styled cells
    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createContentCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createTotalLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createTotalValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
}