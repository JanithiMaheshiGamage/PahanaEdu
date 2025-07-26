<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Online Billing System</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="css/styles.css">
</head>
<body>

<div class="left-section">
    <img src="images/pahana-illustration.png" alt="PAHANA EDU Illustration">
</div>

<div class="right-section">


    <div class="login-form">
        <h2>LOGIN</h2>



        <form method="post" action="login">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" placeholder="Enter your username" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>
            </div>

            <%
                String message = request.getParameter("message");
                String reset = request.getParameter("reset");
            %>

            <% if ("logout".equals(message)) { %>
            <div class="alert alert-success">Logged out successfully.</div>
            <% } else if ("invalid".equals(message)) { %>
            <div class="alert alert-danger">Invalid username or password. Please try again.</div>
            <% } else if ("success".equals(reset)) { %>
            <div class="alert alert-success">Password reset successfully. Please login with your new password.</div>
            <% } %>

            <button type="submit" class="login-btn">LOGIN</button>

            <div class="forgot-password">
                <a href="password_reset.jsp">Forgot Password?</a>
            </div>
        </form>
    </div>
</div>

</body>
</html>
