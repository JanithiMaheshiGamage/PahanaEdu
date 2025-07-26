<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Reset Password</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">

</head>
<body>
<div class="admin-container">
    <!-- Sidebar (hidden on password reset page) -->
    <div class="sidebar" style="display: none;"></div>

    <!-- Main Content -->
    <div class="main-content" style="margin-left: 0; width: 100%;">
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
        </div>

        <!-- Content Wrapper -->
        <div class="content-wrapper" style="max-width: 500px; margin: 0 auto;">
            <h1 class="page-title">Reset Your Password</h1>

            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
            <div class="alert alert-danger" style="color: #dc3545; margin-bottom: 20px;">
                <% if (error.equals("mismatch")) { %>
                Passwords do not match. Please try again.
                <% } else if (error.equals("weak")) { %>
                Password must be at least 8 characters with uppercase, lowercase, and numbers.
                <% } else if (error.equals("database")) { %>
                Error updating password. Please try again later.
                <% } else { %>
                Error resetting password. Please try again.
                <% } %>
            </div>
            <% } %>

            <form method="post" action="resetPassword" class="user-form">
                <input type="hidden" name="username" value="${param.username}">

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

                <a href="login.jsp" class="back-button">
                    <i class="fas fa-arrow-left"></i> Back to Login
                </a>
            </form>
        </div>
    </div>
</div>
</body>
</html>