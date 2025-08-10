<%@ page import="com.pahanaedu.dao.TransactionDAO" %>
<%@ page import="com.pahanaedu.model.Transaction" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
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

    TransactionDAO transactionDAO = new TransactionDAO();
    List<Transaction> transactions = null;

    // Handle search
    String searchKeyword = request.getParameter("search");

    // Admin sees all transactions, staff sees only their own
    if ("admin".equals(role)) {
        transactions = transactionDAO.getAllTransactions(searchKeyword);
    } else {
        transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);
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
    <title>PAHANA EDU - Transaction History</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
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

<div class="admin-container">
    <!-- Sidebar -->
    <% if ("admin".equals(role)) { %>
    <jsp:include page="sidebar.jsp">
        <jsp:param name="activePage" value="transactions" />
    </jsp:include>
    <% } else { %>
    <jsp:include page="staff_sidebar.jsp">
        <jsp:param name="activePage" value="transactions" />
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
            <h1 class="page-title">Transaction History</h1>

            <!-- Search Bar -->
            <form method="get" class="search-bar">
                <input type="text" name="search" placeholder="Search by bill no, customer..."
                       value="<%= searchKeyword != null ? searchKeyword : "" %>">
                <button type="submit"><i class="fas fa-search"></i></button>
                <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
                <a href="transactions.jsp" class="btn btn-secondary" style="margin-left: 10px;">Clear Search</a>
                <% } %>
            </form>

            <!-- Transactions Table -->
            <div class="user-table-container">
                <table class="user-table">
                    <thead>
                    <tr>
                        <th>Bill No</th>
                        <th>Customer</th>
                        <th>Total Amount</th>
                        <th>Payment Method</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if(transactions != null && !transactions.isEmpty()) { %>
                    <% for (Transaction transaction : transactions) { %>
                    <tr>
                        <td><%= transaction.getBillNo() %></td>
                        <td><%= transaction.getCustomerName() %></td>
                        <td>LKR <%= String.format("%.2f", transaction.getTotalAmount()) %></td>
                        <td><%= transaction.getPaymentMethod().toUpperCase() %></td>
                        <td><%= new SimpleDateFormat("yyyy-MM-dd HH:mm").format(transaction.getPaidDate()) %></td>
                        <td>
                <span class="status-badge <%= transaction.getStatus() %>">
                  <%= transaction.getStatus().toUpperCase() %>
                </span>
                        </td>
                        <td>
                            <button class="view-btn" data-id="<%= transaction.getBillId() %>">
                                <i class="fas fa-eye"></i> View
                            </button>
                        </td>
                    </tr>
                    <% } %>
                    <% } else { %>
                    <tr>
                        <td colspan="7" style="text-align: center;">No transactions found</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>

    console.log("Context path: '${pageContext.request.contextPath}'");
    window.contextPath = "${pageContext.request.contextPath}";
    document.addEventListener('DOMContentLoaded', function() {
        // Notification handling
        document.querySelectorAll('.close-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                this.closest('.notification').remove();
            });
        });

        // Auto-close notifications after 5 seconds
        setTimeout(() => {
            document.querySelectorAll('.notification').forEach(notification => {
                notification.remove();
            });
        }, 5000);

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

        // View button functionality
        document.querySelectorAll('.view-btn').forEach(button => {
            button.addEventListener('click', function() {
                const billId = this.getAttribute('data-id');
                const url = '${pageContext.request.contextPath}/view-bill?billId=' + billId;
                console.log("Opening URL:", url);  // Debug output
                window.open(url, '_blank');
            });
        });
    });
</script>
</body>
</html>