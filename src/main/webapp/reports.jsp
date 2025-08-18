<%@ page import="com.pahanaedu.dao.ReportDAO" %>
<%@ page import="com.pahanaedu.model.ReportItem" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.pahanaedu.util.DBConnection" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.Collections" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%!
    private static final Logger logger = LogManager.getLogger("ReportsJSP");
%>
<%
    // Check if user is logged in
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;
    Integer userId = (httpSession != null) ? (Integer) httpSession.getAttribute("userId") : null;

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Initialize variables
    Connection connection = null;
    ReportDAO reportDAO = null;
    String errorMessage = null;

    try {
        connection = DBConnection.getConnection();
        reportDAO = new ReportDAO(connection);
    } catch (SQLException e) {
        errorMessage = "Database connection failed. Please try again later.";
        logger.error("Database connection error", e);
    }

    // Default date range (current month)
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String endDate = dateFormat.format(cal.getTime());
    cal.set(Calendar.DAY_OF_MONTH, 1);
    String startDate = dateFormat.format(cal.getTime());

    // Get parameters if submitted
    String paramStartDate = request.getParameter("startDate");
    String paramEndDate = request.getParameter("endDate");
    String reportType = request.getParameter("reportType");

    if (paramStartDate != null && !paramStartDate.isEmpty()) {
        startDate = paramStartDate;
    }
    if (paramEndDate != null && !paramEndDate.isEmpty()) {
        endDate = paramEndDate;
    }
    if (reportType == null || reportType.isEmpty()) {
        reportType = "sales";
    }

    // Notification variables
    String success = (String) httpSession.getAttribute("success");
    String error = (String) httpSession.getAttribute("error");
    if (success != null) httpSession.removeAttribute("success");
    if (error != null) httpSession.removeAttribute("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Reports</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

</head>
<body>

<!-- Notification Messages -->
<% if (success != null) { %>
<div class="notification success">
    <span><%= success %></span>
    <button class="close-btn">&times;</button>
</div>
<% } %>
<% if (error != null) { %>
<div class="notification error">
    <span><%= error %></span>
    <button class="close-btn">&times;</button>
</div>
<% } %>
<% if (errorMessage != null) { %>
<div class="notification error">
    <span><%= errorMessage %></span>
    <button class="close-btn">&times;</button>
</div>
<% } %>

<div class="admin-container">
    <!-- Sidebar -->
    <% if ("admin".equals(role)) { %>
    <jsp:include page="sidebar.jsp">
        <jsp:param name="activePage" value="reports" />
    </jsp:include>
    <% } else { %>
    <jsp:include page="staff_sidebar.jsp">
        <jsp:param name="activePage" value="reports" />
    </jsp:include>
    <% } %>

    <!-- Main Content -->
    <div class="main-content">
        <!-- Topbar -->
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
            <div class="datetime" id="currentDateTime"></div>
        </div>

        <!-- Content Wrapper -->
        <div class="content-wrapper">
            <h1 class="page-title">Reports Dashboard</h1>

            <!-- Report Filters -->
            <div class="report-filters">
                <form method="get" class="filter-form" id="reportFilterForm">
                    <div class="form-group">
                        <label for="reportType">Report Type</label>
                        <select id="reportType" name="reportType" class="form-control" onchange="this.form.submit()">
                            <option value="sales" <%= "sales".equals(reportType) ? "selected" : "" %>>Sales Transactions</option>
                            <!--<option value="inventory" <%= "inventory".equals(reportType) ? "selected" : "" %>>Inventory Report</option>
                            <option value="customer" <%= "customer".equals(reportType) ? "selected" : "" %>>Customer Transactions</option>
                            <option value="popular" <%= "popular".equals(reportType) ? "selected" : "" %>>Popular Items</option>-->
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="startDate">Start Date</label>
                        <input type="date" id="startDate" name="startDate" class="form-control"
                               value="<%= startDate %>" max="<%= endDate %>">
                    </div>

                    <div class="form-group">
                        <label for="endDate">End Date</label>
                        <input type="date" id="endDate" name="endDate" class="form-control"
                               value="<%= endDate %>" min="<%= startDate %>">
                    </div>

                    <button type="submit" class="btn btn-primary">Generate Report</button>
                    <button type="button" id="exportPdf" class="btn btn-secondary">Export PDF</button>
                </form>
            </div>

            <!-- Report Content -->
            <div class="report-content">
                <% if (reportDAO == null) { %>
                <div class="alert alert-danger">Report service is currently unavailable. Please try again later.</div>
                <% } else if ("sales".equals(reportType)) { %>
                <!-- Sales Transactions Report -->
                <div class="report-section">
                    <h2>Transactions Summary</h2>
                    <div class="summary-cards">
                        <div class="summary-card">
                            <h3>Total Transactions</h3>
                            <p><%= reportDAO.getTransactionCount(startDate, endDate) %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Total Revenue</h3>
                            <p>LKR <%= String.format("%.2f", reportDAO.getTotalRevenue(startDate, endDate)) %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Avg. Transaction Value</h3>
                            <p>LKR <%= String.format("%.2f", reportDAO.getAverageTransactionValue(startDate, endDate)) %></p>
                        </div>
                    </div>

                    <div class="chart-container">
                        <canvas id="salesChart"></canvas>
                    </div>

                    <div class="chart-container">
                        <canvas id="paymentMethodChart"></canvas>
                    </div>

                    <h3>Recent Transactions</h3>
                    <% List<ReportItem> recentTransactions = reportDAO.getRecentTransactions(startDate, endDate, 10); %>
                    <% if (recentTransactions != null && !recentTransactions.isEmpty()) { %>
                    <div class="table-container">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Transaction ID</th>
                                <th>Bill No</th>
                                <th>Customer</th>
                                <th>Amount</th>
                                <th>Payment Method</th>
                                <th>Date</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (ReportItem transaction : recentTransactions) { %>
                            <tr>
                                <td><%= transaction.getTransactionId() %></td>
                                <td><%= transaction.getBillNumber() %></td>
                                <td><%= transaction.getCustomerName() != null ? transaction.getCustomerName() : "N/A" %></td>
                                <td>LKR <%= String.format("%.2f", transaction.getAmount()) %></td>
                                <td><%= transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "N/A" %></td>
                                <td><%= transaction.getTransactionDate() != null ? dateFormat.format(transaction.getTransactionDate()) : "N/A" %></td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% } else { %>
                    <div class="alert alert-info">No transactions found for the selected period.</div>
                    <% } %>
                </div>

                <% } else if ("inventory".equals(reportType)) { %>
                <!-- Inventory Report -->
                <div class="report-section">
                    <h2>Inventory Status</h2>

                    <div class="summary-cards">
                        <div class="summary-card">
                            <h3>Total Items</h3>
                            <p><%= reportDAO.getItemCount() %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Low Stock Items</h3>
                            <p><%= reportDAO.getLowStockItemCount(5) %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Out of Stock</h3>
                            <p><%= reportDAO.getOutOfStockItemCount() %></p>
                        </div>
                    </div>

                    <div class="chart-container">
                        <canvas id="inventoryChart"></canvas>
                    </div>

                    <h3>Low Stock Items</h3>
                    <% List<ReportItem> lowStockItems = reportDAO.getLowStockItems(5); %>
                    <% if (lowStockItems != null && !lowStockItems.isEmpty()) { %>
                    <div class="table-container">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Item ID</th>
                                <th>Name</th>
                                <th>Category</th>
                                <th>Price</th>
                                <th>Stock Qty</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (ReportItem item : lowStockItems) { %>
                            <tr>
                                <td><%= item.getItemId() %></td>
                                <td><%= item.getItemName() != null ? item.getItemName() : "N/A" %></td>
                                <td><%= item.getCategoryName() != null ? item.getCategoryName() : "N/A" %></td>
                                <td>LKR <%= String.format("%.2f", item.getPrice()) %></td>
                                <td class="<%= item.getStockQuantity() == 0 ? "text-danger" : "text-warning" %>">
                                    <%= item.getStockQuantity() %>
                                </td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% } else { %>
                    <div class="alert alert-info">No low stock items found.</div>
                    <% } %>
                </div>

                <% } else if ("customer".equals(reportType)) { %>
                <!-- Customer Transactions Report -->
                <div class="report-section">
                    <h2>Customer Transactions</h2>

                    <div class="summary-cards">
                        <div class="summary-card">
                            <h3>Total Customers</h3>
                            <p><%= reportDAO.getCustomerCount() %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Active Customers</h3>
                            <p><%= reportDAO.getActiveCustomerCount(startDate, endDate) %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Avg. Transactions per Customer</h3>
                            <p><%= String.format("%.1f", reportDAO.getAverageTransactionsPerCustomer(startDate, endDate)) %></p>
                        </div>
                    </div>

                    <div class="chart-container">
                        <canvas id="customerChart"></canvas>
                    </div>

                    <h3>Top Customers by Spending</h3>
                    <% List<ReportItem> topCustomers = reportDAO.getTopSpendingCustomers(startDate, endDate, 10); %>
                    <% if (topCustomers != null && !topCustomers.isEmpty()) { %>
                    <div class="table-container">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Customer ID</th>
                                <th>Name</th>
                                <th>Total Transactions</th>
                                <th>Total Spent</th>
                                <th>Last Transaction</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (ReportItem customer : topCustomers) { %>
                            <tr>
                                <td><%= customer.getCustomerId() %></td>
                                <td><%= customer.getCustomerName() != null ? customer.getCustomerName() : "N/A" %></td>
                                <td><%= customer.getTransactionCount() %></td>
                                <td>LKR <%= String.format("%.2f", customer.getTotalSpent()) %></td>
                                <td><%= customer.getLastTransactionDate() != null ? dateFormat.format(customer.getLastTransactionDate()) : "N/A" %></td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% } else { %>
                    <div class="alert alert-info">No customer data found for the selected period.</div>
                    <% } %>
                </div>

                <% } else if ("popular".equals(reportType)) { %>
                <!-- Popular Items Report -->
                <div class="report-section">
                    <h2>Popular Items</h2>

                    <div class="summary-cards">
                        <div class="summary-card">
                            <h3>Most Sold Item</h3>
                            <p>
                                <% List<ReportItem> topItem = reportDAO.getTopSellingItems(startDate, endDate, 1); %>
                                <%= !topItem.isEmpty() ? topItem.get(0).getItemName() : "N/A" %>
                            </p>
                        </div>
                        <div class="summary-card">
                            <h3>Total Items Sold</h3>
                            <p><%= reportDAO.getTotalItemsSoldCount(startDate, endDate) %></p>
                        </div>
                        <div class="summary-card">
                            <h3>Avg. Items per Transaction</h3>
                            <p><%= String.format("%.1f", reportDAO.getAverageItemsPerTransaction(startDate, endDate)) %></p>
                        </div>
                    </div>

                    <div class="chart-container">
                        <canvas id="popularItemsChart"></canvas>
                    </div>

                    <h3>Top Selling Items</h3>
                    <% List<ReportItem> topItems = reportDAO.getTopSellingItems(startDate, endDate, 10); %>
                    <% if (topItems != null && !topItems.isEmpty()) { %>
                    <div class="table-container">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Item ID</th>
                                <th>Name</th>
                                <th>Category</th>
                                <th>Quantity Sold</th>
                                <th>Total Revenue</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (ReportItem item : topItems) { %>
                            <tr>
                                <td><%= item.getItemId() %></td>
                                <td><%= item.getItemName() != null ? item.getItemName() : "N/A" %></td>
                                <td><%= item.getCategoryName() != null ? item.getCategoryName() : "N/A" %></td>
                                <td><%= item.getQuantitySold() %></td>
                                <td>LKR <%= String.format("%.2f", item.getTotalRevenue()) %></td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% } else { %>
                    <div class="alert alert-info">No sales data found for the selected period.</div>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
    </div>
</div>

<script src="js/reports.js"></script>
<script>
    window.reportData = {
        reportType: '<%= reportType %>',
        startDate: '<%= startDate %>',
        endDate: '<%= endDate %>',
        contextPath: '${pageContext.request.contextPath}'
    };

    // Initialize charts after page loads
    document.addEventListener('DOMContentLoaded', function() {
        // Update current date and time
        function updateDateTime() {
            const now = new Date();
            const options = {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            };
            document.getElementById('currentDateTime').textContent = now.toLocaleString('en-GB', options);
        }
        updateDateTime();
        setInterval(updateDateTime, 60000);

        // Date validation
        document.getElementById('startDate').addEventListener('change', function() {
            document.getElementById('endDate').min = this.value;
        });

        document.getElementById('endDate').addEventListener('change', function() {
            document.getElementById('startDate').max = this.value;
        });
    });
</script>
</body>
</html>

<%
    // Close database connection
    if (connection != null) {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
%>