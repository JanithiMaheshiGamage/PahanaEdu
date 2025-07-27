<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pahanaedu.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Reset Password</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        .password-fields { display: none; }
        .verified .password-fields { display: block; }
        .verified .verification-fields { display: none; }
    </style>
</head>
<body>
<div class="admin-container">
    <div class="sidebar" style="display: none;"></div>
    <div class="main-content" style="margin-left: 0; width: 100%;">
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
        </div>
        <div class="content-wrapper" style="max-width: 500px; margin: 0 auto;">
            <h1 class="page-title">Reset Your Password</h1>

            <%
                String error = request.getParameter("error");
                String success = request.getParameter("success");
                boolean verified = "true".equals(request.getParameter("verified"));
            %>

            <% if (error != null) { %>
            <div class="notification error">
                <% if (error.equals("empty_fields")) { %>
                <span>Username and email are required</span>
                <% } else if (error.equals("invalid_credentials")) { %>
                <span>Username and email don't match our records</span>
                <% } else if (error.equals("password_mismatch")) { %>
                <span>Passwords do not match</span>
                <% } else if (error.equals("weak_password")) { %>
                <span>Password must be at least 8 characters with uppercase, lowercase, and numbers</span>
                <% } else if (error.equals("session_expired")) { %>
                <span>Session expired. Please start over.</span>
                <% } else { %>
                <span>Error processing your request. Please try again.</span>
                <% } %>
                <button class="close-btn">&times;</button>
            </div>
            <% } %>

            <% if (success != null) { %>
            <div class="notification success">
                <span>Password has been reset successfully!</span>
                <button class="close-btn">&times;</button>
            </div>
            <% } %>

            <div class="form-container <%= verified ? "verified" : "" %>">
                <!-- Verification Form (shown first) -->
                <form method="post" action="resetPassword" class="user-form verification-fields">
                    <input type="hidden" name="action" value="verify">

                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username"
                               placeholder="Enter your username" required>
                    </div>

                    <div class="form-group">
                        <label for="email">Registered Email</label>
                        <input type="email" id="email" name="email"
                               placeholder="Enter your registered email" required>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Verify Account</button>
                    </div>
                </form>

                <!-- Password Reset Form (shown after verification) -->
                <form method="post" action="resetPassword" class="user-form password-fields"
                      onsubmit="return validatePassword()">
                    <input type="hidden" name="action" value="reset">

                    <div class="form-group">
                        <label for="newPassword">New Password</label>
                        <input type="password" id="newPassword" name="newPassword"
                               placeholder="Enter new password" required>
                        <small class="form-text">Must be at least 8 characters with uppercase, lowercase, and numbers</small>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword"
                               placeholder="Confirm new password" required>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Reset Password</button>
                    </div>
                </form>
            </div>

            <a href="login.jsp" class="back-button">
                <i class="fas fa-arrow-left"></i> Back to Login
            </a>
        </div>
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
    });

    function validatePassword() {
        const password = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (password !== confirmPassword) {
            alert('Passwords do not match!');
            return false;
        }

        if (password.length < 8 ||
            !/[A-Z]/.test(password) ||
            !/[a-z]/.test(password) ||
            !/[0-9]/.test(password)) {
            alert('Password must be at least 8 characters with uppercase, lowercase, and numbers');
            return false;
        }

        return true;
    }
</script>
</body>
</html>