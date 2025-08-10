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
    <title>PAHANA EDU - Admin Help</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>

<div class="admin-container">
    <!-- Sidebar -->
    <jsp:include page="sidebar.jsp">
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
            <h1 class="page-title">Admin Help Center</h1>

            <div class="help-section">
                <h2>System Administration</h2>
                <div class="help-card">
                    <h3><i class="fas fa-users-cog"></i> User Management</h3>
                    <p>Create, edit, and manage system users and their permissions.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-boxes"></i> Inventory Management</h3>
                    <p>Add, edit, and track inventory items and categories.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-chart-line"></i> Reports</h3>
                    <p>Generate system reports and view analytics.</p>
                </div>
            </div>

            <div class="help-section">
                <h2>Common Issues</h2>
                <div class="help-card">
                    <h3><i class="fas fa-question-circle"></i> Password Reset</h3>
                    <p>How to reset passwords for staff users.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-database"></i> Data Backup</h3>
                    <p>Instructions for system backup procedures.</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>