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
                    <p>System users can be added by providing a valid Full Name, unique Username, Password (minimum 8 characters with uppercase, lowercase, and numbers), unique Email, Employee Number, User Role (Staff/Admin), and User Status (Active/Inactive).
                        Use the <b>Clear</b> button to reset input fields. By clicking the <b>Add</b> button, the user will be added to the system.
                        Existing users can be searched by User ID, Full Name, Employee Number, or Username.
                        By clicking the <b>Edit</b> button, you can update selected user details, and by clicking the <b>Delete</b> button, you can remove the selected user from the system.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-user-friends"></i> Customer Management</h3>
                    <p>Customers can be added by providing a valid Full Name, NIC, Phone Number, and Email Address.
                        Use the <b>Clear</b> button to reset input fields. By clicking the <b>Add Customer</b> button, the customer will be added to the system.
                        Existing customers can be searched by Customer Name, NIC, or Account Number.
                        By clicking the <b>Edit</b> button, you can update selected customer details, and by clicking the <b>Delete</b> button, you can remove the selected customer from the system.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-boxes"></i> Inventory Management</h3>
                    <p>Items can be added by providing Item Name, Category, Unit Price, Stock Quantity, and a Description.
                        If a required category does not exist, click the <b>Add Category</b> button to create a new one.
                        Use the <b>Clear</b> button to reset input fields. By clicking the <b>Add Item</b> button, the item will be added to the system.
                        Existing items can be searched by Item Name or Item ID.
                        By clicking the <b>Edit</b> button, you can update selected item details, and by clicking the <b>Delete</b> button, you can remove the selected item.
                        All categories can also be viewed and deleted if necessary.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-exchange-alt"></i> Transactions</h3>
                    <p>Transactions can be searched by entering Bill Number or Customer Name.
                        By clicking the <b>View</b> button, the corresponding bill can be viewed.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-chart-line"></i> Reports</h3>
                    <p>In the Reports section, the Admin can view summarized details and dashboards for Sales Transactions, Inventory Reports, Customer Transactions, and Popular Items.
                        Reports can also be downloaded for record-keeping and analysis.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-question-circle"></i> Help</h3>
                    <p>If you need any assistance, you can always visit the Help section. You are currently here!</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-user"></i> Profile</h3>
                    <p>You can view your profile details. Only Full Name and Email can be updated.
                        You can also change your password from here. Additionally, on the login page, there is a <b>Forgot Password</b> option to reset your password.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-sign-out-alt"></i> Logout</h3>
                    <p>When you click the <b>Logout</b> button, you will be redirected back to the Login screen.</p>
                </div>
            </div>

            <!--Issues Section -->
            <div class="help-section">
                <h2>Issues</h2>
                <div class="help-card">
                    <h3><i class="fas fa-envelope"></i> Contact Support</h3>
                    <p>If you face any issues or concerns, please contact the company via email. - pahanaeduhelp@gmail.com </p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>