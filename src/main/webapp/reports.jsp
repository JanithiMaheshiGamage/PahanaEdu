<%@ page import="com.pahanaedu.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;

    if (username == null || !"admin".equals(role)) {
        response.sendRedirect("login.jsp");
        return;
    }
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
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>

<div class="admin-container">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="activePage" value="reports" />
    </jsp:include>

    <div class="main-content">
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
            <div class="datetime" id="currentDateTime"></div>
        </div>

        <div class="content-wrapper">
            <h1 class="page-title">Reports Dashboard</h1>

            <div class="report-filters">
                <form id="reportFilterForm">
                    <div class="form-row">

                        <div class="form-group">
                            <label for="startDate">From</label>
                            <input type="date" id="startDate" name="startDate" class="form-control">
                        </div>
                        <div class="form-group">
                            <label for="endDate">To</label>
                            <input type="date" id="endDate" name="endDate" class="form-control">
                        </div>
                        <div class="form-group">
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-filter"></i> Apply Filters
                            </button>
                            <button type="button" id="exportBtn" class="btn btn-secondary">
                                <i class="fas fa-file-export"></i> Export
                            </button>
                        </div>
                    </div>
                </form>
            </div>

            <div class="report-cards">
                <div class="report-card">
                    <div class="report-card-header">
                        <h3><i class="fas fa-chart-line"></i> Sales Summary</h3>
                        <span class="report-period" id="salesPeriod">Last 30 Days</span>
                    </div>
                    <div class="report-card-body">
                        <div class="report-metric">
                            <span class="metric-value" id="totalRevenue">
                                LKR <fmt:formatNumber value="${salesSummary.totalRevenue}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                            </span>
                            <span class="metric-label">Total Revenue</span>
                        </div>
                        <div class="report-metric">
                            <span class="metric-value" id="transactionCount">
                                <fmt:formatNumber value="${salesSummary.transactionCount}" type="number"/>
                            </span>
                            <span class="metric-label">Transactions</span>
                        </div>
                        <div class="report-chart">
                            <canvas id="salesChart" height="150"></canvas>
                        </div>
                    </div>
                </div>

                <div class="report-card">
                    <div class="report-card-header">
                        <h3><i class="fas fa-boxes"></i> Inventory Status</h3>
                        <span class="report-period">Current Stock</span>
                    </div>
                    <div class="report-card-body">
                        <div class="report-metric">
                            <span class="metric-value">42</span>
                            <span class="metric-label">Low Stock Items</span>
                        </div>
                        <div class="report-metric">
                            <span class="metric-value">1,248</span>
                            <span class="metric-label">Total Items</span>
                        </div>
                        <div class="report-chart">
                            <canvas id="inventoryChart" height="150"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <div class="detailed-reports">
                <h2 class="section-title">Detailed Reports</h2>

                <div class="report-section">
                    <h3>Sales by Category</h3>
                    <div class="table-responsive">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Category</th>
                                <th>Items Sold</th>
                                <th>Total Revenue</th>
                                <th>% of Total</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>Books</td>
                                <td>156</td>
                                <td>LKR 78,400</td>
                                <td>32%</td>
                            </tr>
                            <tr>
                                <td>Stationery</td>
                                <td>89</td>
                                <td>LKR 42,300</td>
                                <td>17%</td>
                            </tr>
                            <tr>
                                <td>Electronics</td>
                                <td>45</td>
                                <td>LKR 125,100</td>
                                <td>51%</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="report-section">
                    <h3>Top Selling Items</h3>
                    <div class="table-responsive">
                        <table class="report-table">
                            <thead>
                            <tr>
                                <th>Item</th>
                                <th>Category</th>
                                <th>Quantity Sold</th>
                                <th>Revenue</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>Advanced Mathematics</td>
                                <td>Books</td>
                                <td>42</td>
                                <td>LKR 25,200</td>
                            </tr>
                            <tr>
                                <td>Wireless Mouse</td>
                                <td>Electronics</td>
                                <td>38</td>
                                <td>LKR 45,600</td>
                            </tr>
                            <tr>
                                <td>Premium Notebook</td>
                                <td>Stationery</td>
                                <td>35</td>
                                <td>LKR 17,500</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/reports.js"></script>
</body>
</html>