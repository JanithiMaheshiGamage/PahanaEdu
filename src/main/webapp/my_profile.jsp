<%@ page import="com.pahanaedu.dao.UserDAO" %>
<%@ page import="com.pahanaedu.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Check if user is logged in
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;
    String fullName = (httpSession != null) ? (String) httpSession.getAttribute("fullName") : null;

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    UserDAO userDAO = new UserDAO();
    User currentUser = userDAO.getUserByUsername(username);

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
    <title>PAHANA EDU - My Profile</title>
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
        <jsp:param name="activePage" value="profile" />
    </jsp:include>
    <% } else { %>
    <jsp:include page="staff_sidebar.jsp">
        <jsp:param name="activePage" value="profile" />
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
            <h1 class="page-title">My Profile</h1>

            <!-- Profile Form -->
            <form method="post" class="user-form" id="profileForm"
                  action="${pageContext.request.contextPath}/update-profile"
                  enctype="application/x-www-form-urlencoded">
                <input type="hidden" name="userId" value="<%= currentUser.getId() %>">

                <div class="form-row">
                    <div class="form-group">
                        <label for="name">Full Name</label>
                        <input type="text" id="name" name="name" value="<%= currentUser.getFullName() %>" required>
                    </div>
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" class="readonly-field" value="<%= currentUser.getUsername() %>" readonly>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" value="<%= currentUser.getEmail() %>" required>
                    </div>
                    <div class="form-group">
                        <label for="employeeNo">Employee Number</label>
                        <input type="text" id="employeeNo" class="readonly-field" value="<%= currentUser.getEmployeeNo() %>" readonly>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="role">User Role</label>
                        <input type="text" id="role" class="readonly-field" value="<%= currentUser.getRole() %>" readonly>
                    </div>
                    <div class="form-group">
                        <label>Status</label>
                        <input type="text" class="readonly-field" value="<%= currentUser.isStatus() ? "Active" : "Inactive" %>" readonly>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary" id="updateProfileBtn">Update Profile</button>
                    <button type="button" class="btn btn-secondary" id="changePasswordBtn">Change Password</button>
                </div>
            </form>

            <!-- Change Password Modal (hidden by default) -->
            <div id="passwordModal" class="modal" style="display:none;">
                <div class="modal-content">
                    <span class="close-modal">&times;</span>
                    <h2>Change Password</h2>
                    <form id="passwordForm" action="${pageContext.request.contextPath}/change-password" method="post">
                        <input type="hidden" name="userId" value="<%= currentUser.getId() %>">

                        <div class="form-group">
                            <label for="currentPassword">Current Password</label>
                            <input type="password" id="currentPassword" name="currentPassword" required>
                        </div>

                        <div class="form-group">
                            <label for="newPassword">New Password</label>
                            <input type="password" id="newPassword" name="newPassword" required>
                            <small class="form-text">Must be at least 8 characters with uppercase, lowercase, and numbers</small>
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">Confirm New Password</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" required>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" id="updatePasswordBtn">Update Password</button>
                            <button type="button" class="btn btn-secondary" id="cancelPasswordChange">Cancel</button>
                        </div>
                    </form>
                </div>
            </div>
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

        // Password change modal functionality
        const passwordModal = document.getElementById('passwordModal');
        const changePasswordBtn = document.getElementById('changePasswordBtn');
        const closeModal = document.querySelector('.close-modal');
        const cancelPasswordChange = document.getElementById('cancelPasswordChange');

        changePasswordBtn.addEventListener('click', function() {
            passwordModal.style.display = 'block';
        });

        closeModal.addEventListener('click', function() {
            passwordModal.style.display = 'none';
            document.getElementById('passwordForm').reset();
        });

        cancelPasswordChange.addEventListener('click', function() {
            passwordModal.style.display = 'none';
            document.getElementById('passwordForm').reset();
        });

        // Close modal when clicking outside of it
        window.addEventListener('click', function(event) {
            if (event.target === passwordModal) {
                passwordModal.style.display = 'none';
                document.getElementById('passwordForm').reset();
            }
        });

        // Update Profile Button functionality
        const updateProfileBtn = document.getElementById('updateProfileBtn');
        const profileForm = document.getElementById('profileForm');

        profileForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // Validate form first
            if (!validateProfileForm()) {
                return false;
            }

            // Show loading state
            updateProfileBtn.disabled = true;
            updateProfileBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';

            // Convert form data to URL-encoded format
            const formData = new URLSearchParams(new FormData(profileForm));

            // Submit form
            fetch(profileForm.action, {
                method: 'POST',
                body: formData,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                    } else if (!response.ok) {
                        return response.text().then(text => { throw new Error(text) });
                    }
                    return response.text();
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Update failed: ' + error.message);
                })
                .finally(() => {
                    updateProfileBtn.disabled = false;
                    updateProfileBtn.innerHTML = 'Update Profile';
                });
        });


        function validateProfileForm() {
            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();

            if (!name) {
                alert('Full name is required');
                return false;
            }

            if (!email) {
                alert('Email is required');
                return false;
            }

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                alert('Please enter a valid email address');
                return false;
            }

            return true;
        }

        // Password change form validation
        document.getElementById('passwordForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const updatePasswordBtn = document.getElementById('updatePasswordBtn');

            if (!currentPassword || !newPassword || !confirmPassword) {
                alert('Please fill all password fields');
                return false;
            }

            if (newPassword.length < 8 ||
                !/[A-Z]/.test(newPassword) ||
                !/[a-z]/.test(newPassword) ||
                !/[0-9]/.test(newPassword)) {
                alert('New password must be at least 8 characters with uppercase, lowercase, and numbers');
                return false;
            }

            if (newPassword !== confirmPassword) {
                alert('New password and confirmation do not match');
                return false;
            }

            // Show loading state
            updatePasswordBtn.disabled = true;
            updatePasswordBtn.classList.add('btn-loading');
            updatePasswordBtn.textContent = 'Updating...';

            // Submit form asynchronously
            fetch(this.action, {
                method: 'POST',
                body: new FormData(this)
            })
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                    } else {
                        return response.text();
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    updatePasswordBtn.disabled = false;
                    updatePasswordBtn.classList.remove('btn-loading');
                    updatePasswordBtn.textContent = 'Update Password';
                    alert('An error occurred while changing your password');
                });
        });
    });
</script>
</body>
</html>