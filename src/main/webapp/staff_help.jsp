<%@ page import="com.pahanaedu.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;

    if (username == null || !"staff".equals(role)) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Staff Help</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>

<div class="admin-container">
    <!-- Sidebar -->
    <jsp:include page="staff_sidebar.jsp">
        <jsp:param name="activePage" value="help" />
    </jsp:include>

    <!-- Main Content -->
    <div class="main-content">
        <!-- Topbar -->
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
            <div class="datetime" id="currentDateTime"></div>
        </div>

        <!-- Content Wrapper -->
        <div class="content-wrapper">
            <h1 class="page-title">Staff Help Center</h1>

            <div class="help-section">
                <h2>Daily Operations</h2>
                <div class="help-card">
                    <h3><i class="fas fa-cash-register"></i> Billing System</h3>
                    <p>How to process customer transactions and generate bills.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-user-plus"></i> Customer Management</h3>
                    <p>Adding and managing customer accounts.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-search"></i> Inventory Lookup</h3>
                    <p>How to search for items and check stock levels.</p>
                </div>
            </div>

            <div class="help-section">
                <h2>Troubleshooting</h2>
                <div class="help-card">
                    <h3><i class="fas fa-print"></i> Printing Issues</h3>
                    <p>Solutions for common printing problems.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-key"></i> Login Problems</h3>
                    <p>What to do if you can't log in to the system.</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>