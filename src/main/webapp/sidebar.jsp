<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String fullName = (String) session.getAttribute("fullName");
    String role = (String) session.getAttribute("role");
    String activePage = request.getParameter("activePage") != null ?
            request.getParameter("activePage") : "users";
%>

<div class="sidebar">
    <div class="user-profile">
        <img src="${pageContext.request.contextPath}/images/UserImage.png" alt="User Avatar" class="user-avatar">
        <div class="user-name"><%= fullName != null ? fullName : "Admin User" %></div>
        <div class="user-role"><%= role != null ? role : "Admin" %></div>
    </div>

    <div class="nav-menu">
        <a href="admin_manage_users.jsp"
           class="nav-item <%= "users".equals(activePage) ? "active" : "" %>"
           data-page="users">
            <i class="fas fa-users"></i>
            <span>System Users</span>
        </a>
        <a href="admin_manage_customers.jsp"
           class="nav-item <%= "customers".equals(activePage) ? "active" : "" %>"
           data-page="customers">
            <i class="fas fa-user-tag"></i>
            <span>Customers</span>
        </a>
        <a href="admin_manage_items.jsp"
           class="nav-item <%= "items".equals(activePage) ? "active" : "" %>"
           data-page="items">
            <i class="fas fa-boxes"></i>
            <span>Items</span>
        </a>
        <a href="transactions.jsp"
           class="nav-item <%= "transactions".equals(activePage) ? "active" : "" %>"
           data-page="transactions">
            <i class="fas fa-exchange-alt"></i>
            <span>Transactions</span>
        </a>
        <a href="reports.jsp"
           class="nav-item <%= "reports".equals(activePage) ? "active" : "" %>"
           data-page="reports">
            <i class="fas fa-chart-bar"></i>
            <span>Reports</span>
        </a>
        <a href="help.jsp"
           class="nav-item <%= "help".equals(activePage) ? "active" : "" %>"
           data-page="help">
            <i class="fas fa-question-circle"></i>
            <span>Help</span>
        </a>
        <a href="profile.jsp"
           class="nav-item <%= "profile".equals(activePage) ? "active" : "" %>"
           data-page="profile">
            <i class="fas fa-user-circle"></i>
            <span>My Profile</span>
        </a>
    </div>

    <a href="${pageContext.request.contextPath}/logout" class="logout-btn">
        <i class="fas fa-sign-out-alt"></i>
        <span>Logout</span>
    </a>
</div>