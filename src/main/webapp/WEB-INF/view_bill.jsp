<%@ page import="com.pahanaedu.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Bill bill = (Bill) request.getAttribute("bill");
    Customer customer = (Customer) request.getAttribute("customer");
    List<BillItem> items = (List<BillItem>) request.getAttribute("items");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bill Details - <%= bill.getBillNo() %></title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .bill-header { text-align: center; margin-bottom: 20px; }
        .bill-info { margin-bottom: 20px; }
        .bill-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        .bill-table th, .bill-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .bill-table th { background-color: #f2f2f2; }
        .bill-total { text-align: right; font-weight: bold; margin-top: 10px; }
        .bill-footer { margin-top: 30px; text-align: center; font-size: 0.9em; }
    </style>
</head>
<body>
<div class="bill-header">
    <h1>PAHANA EDU</h1>
    <h2>INVOICE</h2>
</div>

<div class="bill-info">
    <table style="width: 100%; margin-bottom: 20px;">
        <tr>
            <td style="width: 50%; vertical-align: top;">
                <strong>Bill No:</strong> <%= bill.getBillNo() %><br>
                <strong>Date:</strong> <%= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(bill.getCreatedDate()) %>
            </td>
            <td style="width: 50%; vertical-align: top;">
                <% if (customer != null) { %>
                <strong>Customer:</strong> <%= customer.getName() %><br>
                <strong>Account No:</strong> <%= customer.getAccountNo() %><br>
                <strong>Phone:</strong> <%= customer.getPhoneNo() %>
                <% } %>
            </td>
        </tr>
    </table>
</div>

<table class="bill-table">
    <thead>
    <tr>
        <th>Item</th>
        <th>Price</th>
        <th>Qty</th>
        <th>Subtotal</th>
    </tr>
    </thead>
    <tbody>
    <% for (BillItem item : items) { %>
    <tr>
        <td><%= item.getItemName() %></td>
        <td>LKR <%= String.format("%.2f", item.getPrice()) %></td>
        <td><%= item.getQuantity() %></td>
        <td>LKR <%= String.format("%.2f", item.getSubtotal()) %></td>
    </tr>
    <% } %>
    </tbody>
</table>

<div class="bill-total">
    <p>Total: LKR <%= String.format("%.2f", bill.getTotalAmount()) %></p>
</div>

<div class="payment-info">
    <p><strong>Payment Method:</strong> <%= bill.getPaymentMethod().toUpperCase() %></p>
    <p><strong>Payment Details:</strong> <%= bill.getPaymentDetails() %></p>
</div>

<div class="bill-footer">
    <p>Thank you for your business!</p>
    <p>Terms & Conditions: Goods sold are not returnable.</p>
</div>

<div style="text-align: center; margin-top: 20px;">
    <button onclick="window.print()">Print Bill</button>
    <button onclick="window.close()">Close</button>
</div>
</body>
</html>