<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Online Billing System</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>

<div class="left-section">
    <img src="images/pahana-illustration.png" alt="PAHANA EDU Illustration">
</div>

<div class="right-section">
    <div class="login-form">
        <h2>LOGIN</h2>

        <%
            String message = request.getParameter("message");
            String reset = request.getParameter("reset");
            String success = request.getParameter("success");
        %>

        <!-- Notification Messages -->
        <% if ("logout".equals(message)) { %>
        <div class="notification success">
            <span>Logged out successfully</span>
            <button class="close-btn">&times;</button>
        </div>
        <% } else if ("invalid".equals(message)) { %>
        <div class="notification error">
            <span>Invalid username or password. Please try again.</span>
            <button class="close-btn">&times;</button>
        </div>
        <% } else if ("empty".equals(message)) { %>
        <div class="notification error">
            <span>Username and password are required</span>
            <button class="close-btn">&times;</button>
        </div>
        <% } else if ("error".equals(message)) { %>
        <div class="notification error">
            <span>An error occurred. Please try again.</span>
            <button class="close-btn">&times;</button>
        </div>
        <% } else if ("success".equals(reset)) { %>
        <div class="notification success">
            <span>Password reset successfully. Please login with your new password.</span>
            <button class="close-btn">&times;</button>
        </div>
        <% } %>


        <form method="post" action="login">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" placeholder="Enter your username" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>
            </div>

            <button type="submit" class="login-btn">LOGIN</button>

            <div class="forgot-password">
                <a href="password_reset.jsp">Forgot Password?</a>
            </div>
        </form>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Notification handling
        document.querySelectorAll('.close-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const notification = this.closest('.notification');
                notification.classList.add('fade-out');
                setTimeout(() => notification.remove(), 500);
            });
        });

        // Auto-close notifications after 5 seconds
        setTimeout(() => {
            document.querySelectorAll('.notification').forEach(notification => {
                notification.classList.add('fade-out');
                setTimeout(() => notification.remove(), 500);
            });
        }, 5000);

        // Focus on username field when page loads
        document.getElementById('username').focus();

    });
</script>

</body>
</html>