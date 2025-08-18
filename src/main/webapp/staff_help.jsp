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
                    <p>First, check if the customer is already in the system by searching with the phone number.
                        If the customer exists, select them. If not, click the <b>Add Customer</b> button and add their details.
                        Next, add the customer’s selected items. You can search items by <b>Item ID</b> or <b>Item Name</b>.
                        Enter the quantity and click the <b>Add</b> button to add items to the list.
                        You can clear the entire list at any time using the <b>Clear List</b> button.
                        After adding all selected items, you can view the total.
                        Then, choose the payment method: <b>Cash</b> or <b>Card</b>.
                        - If Cash: enter the amount received in LKR.
                        - If Card: enter the card details and confirm the bill. </p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-user-plus"></i> Customer Management</h3>
                    <p>Customers can be added by providing a valid Full Name, NIC, Phone Number, and Email Address.
                        Use the <b>Clear</b> button to reset input fields. By clicking the <b>Add Customer</b> button, the customer will be added to the system.
                        Existing customers can be searched by Customer Name, NIC, or Account Number.
                        By clicking the <b>Edit</b> button, you can update selected customer details, and by clicking the <b>Delete</b> button, you can remove the selected customer from the system.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-search"></i> Inventory Lookup</h3>
                    <p>You can search for inventory items by Item Name or Item ID and check stock levels.
                        Staff members cannot add or delete items but can view availability to assist with customer purchases.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-exchange-alt"></i> Transactions</h3>
                    <p>Transactions can be searched by entering Bill Number or Customer Name.
                        By clicking the <b>View</b> button, the corresponding bill details can be displayed.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-question-circle"></i> Help</h3>
                    <p>If you need any assistance, you can always visit the Help section. You are currently here!</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-user"></i> My Profile</h3>
                    <p>You can view your profile details. Only Full Name and Email can be updated.
                        You can also change your password from here. Additionally, on the login page, there is a <b>Forgot Password</b> option to reset your password.</p>
                </div>

                <div class="help-card">
                    <h3><i class="fas fa-sign-out-alt"></i> Logout</h3>
                    <p>When you click the <b>Logout</b> button, you will be redirected back to the Login screen.</p>
                </div>
            </div>

            <div class="help-section">
                <h2>Issues</h2>
                <div class="help-card">
                    <h3><i class="fas fa-print"></i> Contact Support</h3>
                    <p>If you face any issues or concerns, please contact the company via email. - pahanaeduhelp@gmail.com</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/common.js"></script>
</body>
</html>