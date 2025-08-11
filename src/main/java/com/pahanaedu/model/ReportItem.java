package com.pahanaedu.model;

import java.util.Date;

public class ReportItem {
    // Common fields
    private Object id;
    private String name;
    private String reference;
    private String category;
    private double amount;
    private int quantity;
    private Date date;
    private String paymentMethod;

    // Constructor for transactions
    public ReportItem(Object id, String reference, String name, double amount, String paymentMethod, Date date) {
        this.id = id;
        this.reference = reference;
        this.name = name;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.date = date;
    }

    // Constructor for inventory items
    public ReportItem(Object id, String name, String category, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = price;
        this.quantity = quantity;
    }

    // Constructor for customer reports
    public ReportItem(Object id, String name, double totalSpent, int transactionCount, Date lastTransaction) {
        this.id = id;
        this.name = name;
        this.amount = totalSpent;
        this.quantity = transactionCount;
        this.date = lastTransaction;
    }

    // Getters for all fields
    public Object getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public int getQuantity() {
        return quantity;
    }

    public Date getDate() {
        return date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Specialized getters for JSP
    public int getTransactionId() {
        return (id instanceof Integer) ? (Integer) id : 0;
    }

    public String getBillNumber() {
        return reference;
    }

    public String getCustomerName() {
        return name;
    }

    public String getCustomerId() {
        return (id instanceof String) ? (String) id : "";
    }

    public int getTransactionCount() {
        return quantity;
    }

    public double getTotalSpent() {
        return amount;
    }

    public Date getLastTransactionDate() {
        return date;
    }

    public Date getTransactionDate() {
        return date;
    }

    public int getItemId() {
        return (id instanceof Integer) ? (Integer) id : 0;
    }

    public String getItemName() {
        return name;
    }

    public String getCategoryName() {
        return category;
    }

    public int getStockQuantity() {
        return quantity;
    }

    public int getQuantitySold() {
        return quantity;
    }

    public double getTotalRevenue() {
        return amount;
    }

    public double getPrice() {
        return amount;
    }
}