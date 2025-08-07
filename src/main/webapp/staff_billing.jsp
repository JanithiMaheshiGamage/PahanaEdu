<%@ page import="com.pahanaedu.model.Customer" %>
<%@ page import="com.pahanaedu.model.Item" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Check if user is logged in
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;
    String fullName = (httpSession != null) ? (String) httpSession.getAttribute("fullName") : null;

    if (username == null || !"staff".equals(role)) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Notification variables
    String success = (String) httpSession.getAttribute("success");
    String error = (String) httpSession.getAttribute("error");
    if (success != null) httpSession.removeAttribute("success");
    if (error != null) httpSession.removeAttribute("error");

    // Get selected customer from session if exists
    Customer selectedCustomer = (Customer) httpSession.getAttribute("selectedCustomer");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Staff Billing</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body class="staff-billing">

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
    <jsp:include page="staff_sidebar.jsp">
        <jsp:param name="activePage" value="billing" />
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
            <h1 class="page-title">Staff Billing</h1>

            <!-- Customer Section -->
            <div class="customer-section">
                <div class="customer-flow">
                    <div class="customer-step active">
                        <h2>Customer Information</h2>
                        <div class="search-container">
                            <div class="search-input-group">
                                <i class="fas fa-search search-icon"></i>
                                <input type="text" id="customerPhone" name="customerPhone" placeholder="Enter customer phone number">
                            </div>
                            <div class="search-actions">
                                <button type="button" class="btn btn-secondary" id="checkCustomerBtn">
                                    <i class="fas fa-check"></i> Check
                                </button>
                                <button type="button" class="btn btn-primary" id="addCustomerBtn">
                                    <i class="fas fa-user-plus"></i> Add Customer
                                </button>
                            </div>
                        </div>

                        <!-- Customer Search Results -->
                        <div class="search-results" id="customerSearchResults" style="display: none;">
                            <div class="search-placeholder">
                                <i class="fas fa-search"></i>
                                <p>Enter phone number to search for customer</p>
                            </div>
                        </div>

                        <!-- Selected Customer Card -->
                        <% if (selectedCustomer != null) { %>
                        <div class="customer-card selected" id="selectedCustomerCard">
                            <div class="customer-header">
                                <div class="customer-avatar">
                                    <i class="fas fa-user"></i>
                                </div>
                                <div class="customer-info">
                                    <h3><%= selectedCustomer.getName() %></h3>
                                    <div class="customer-meta">
                                        <span><i class="fas fa-id-card"></i> <%= selectedCustomer.getNic() %></span>
                                        <span><i class="fas fa-phone"></i> <%= selectedCustomer.getPhoneNo() %></span>
                                    </div>
                                </div>
                            </div>
                            <div class="customer-details">
                                <div class="detail-row">
                                    <div class="detail-label">Account No:</div>
                                    <div class="detail-value"><%= selectedCustomer.getAccountNo() %></div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">Email:</div>
                                    <div class="detail-value"><%= selectedCustomer.getEmail() %></div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">Address:</div>
                                    <div class="detail-value"><%= selectedCustomer.getAddress() %></div>
                                </div>
                            </div>
                            <div class="customer-actions">
                                <button type="button" class="btn btn-secondary" id="clearCustomerBtn">
                                    <i class="fas fa-times"></i> Clear Selection
                                </button>
                            </div>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>

            <!-- Items Section -->
            <div class="items-section">
                <h2>Add Items</h2>
                <div class="form-row">
                    <div class="form-group" style="flex: 2;">
                        <label for="itemSearch">Search Item</label>
                        <select id="itemSearch" class="item-search-select" style="width: 100%;">
                            <option value="">Search by item ID or name</option>
                            <!-- Items will be populated via JavaScript -->
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="itemQuantity">Quantity</label>
                        <input type="number" id="itemQuantity" name="itemQuantity" min="1" value="1">
                    </div>
                    <div class="form-group" style="align-self: flex-end;">
                        <button type="button" class="btn btn-primary" id="addItemBtn">
                            <i class="fas fa-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>

            <!-- Cart Section -->
            <div class="cart-section">
                <div class="section-header">
                    <h2>Billing Items</h2>

                </div>
                <button type="button" class="btn btn-secondary" id="clearCartBtn">
                    <i class="fas fa-trash"></i> Clear List
                </button>
                <div class="table-responsive-container">
                    <table class="cart-table">
                        <thead>
                        <tr>
                            <th>Item ID</th>
                            <th>Item Name</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Quantity</th>
                            <th>Subtotal</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody id="cartItems">
                        <!-- Cart items will be populated via JavaScript -->
                        <tr>
                            <td colspan="7" style="text-align: center;">No items added yet</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="total-section">
                    <div class="total-row">
                        <span>Total:</span>
                        <span id="cartTotal">LKR 0.00</span>
                    </div>
                </div>
            </div>

            <!-- Payment Section -->
            <div class="payment-section">
                <div class="payment-method">
                    <h3>Payment Method</h3>
                    <div class="form-group">
                        <div class="payment-options">
                            <label class="payment-option">
                                <input type="radio" name="paymentMethod" value="cash" checked>
                                <span>Cash</span>
                            </label>
                            <label class="payment-option">
                                <input type="radio" name="paymentMethod" value="card">
                                <span>Card</span>
                            </label>
                        </div>
                    </div>

                    <!-- Cash Payment Details -->
                    <div class="payment-details" id="cashPaymentDetails">
                        <div class="form-group">
                            <label for="amountReceived">Amount Received (LKR)</label>
                            <input type="number" id="amountReceived" name="amountReceived" min="0" step="0.01">
                        </div>
                        <div class="form-group">
                            <label>Change (LKR)</label>
                            <div id="changeAmount">0.00</div>
                        </div>
                    </div>

                    <!-- Card Payment Details -->
                    <div class="payment-details" id="cardPaymentDetails" style="display: none;">
                        <div class="form-group">
                            <label for="cardNumber">Card Number</label>
                            <input type="text" id="cardNumber" name="cardNumber" placeholder="1234 5678 9012 3456">
                        </div>
                        <div class="form-group">
                            <label for="cardHolder">Card Holder Name</label>
                            <input type="text" id="cardHolder" name="cardHolder" placeholder="John Doe">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="expiryDate">Expiry Date</label>
                                <input type="text" id="expiryDate" name="expiryDate" placeholder="MM/YY">
                            </div>
                            <div class="form-group">
                                <label for="cvv">CVV</label>
                                <input type="text" id="cvv" name="cvv" placeholder="123">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons">
                <button type="button" class="btn btn-secondary" id="cancelBillBtn">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="button" class="btn btn-primary" id="confirmBillBtn">
                    <i class="fas fa-check"></i> Confirm Bill
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Add Customer Modal -->
<div id="addCustomerModal" class="modal">
    <div class="modal-content">
        <span class="close-modal">&times;</span>
        <h3>Add New Customer</h3>
        <form id="customerForm">
            <div class="form-row">
                <div class="form-group">
                    <label for="newCustomerName">Full Name</label>
                    <input type="text" id="newCustomerName" name="newCustomerName" placeholder="Enter full name" required autocomplete="off">
                </div>
                <div class="form-group">
                    <label for="newCustomerPhone">Phone Number</label>
                    <input type="text" id="newCustomerPhone" name="newCustomerPhone" placeholder="Enter phone number" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="newCustomerNIC">NIC Number</label>
                    <input type="text" id="newCustomerNIC" name="newCustomerNIC" placeholder="Enter NIC number">
                </div>
                <div class="form-group">
                    <label for="newCustomerEmail">Email</label>
                    <input type="email" id="newCustomerEmail" name="newCustomerEmail" placeholder="Enter email">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group" style="flex: 1 0 100%;">
                    <label for="newCustomerAddress">Address</label>
                    <textarea id="newCustomerAddress" name="newCustomerAddress" rows="3" placeholder="Enter address"></textarea>
                </div>
            </div>

            <div class="form-actions">
                <button type="button" class="btn btn-secondary close-modal cancel-btn">Cancel</button>
                <button type="submit" class="btn btn-primary">
                    <span id="saveCustomerText">Save Customer</span>
                    <span id="saveCustomerLoading" style="display: none;">
            <i class="fas fa-spinner fa-spin"></i> Saving...
        </span>
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Bill Confirmation Modal -->
<div id="billConfirmationModal" class="modal">
    <div class="modal-content">
        <span class="close-modal">&times;</span>
        <h3>Confirm Bill</h3>
        <div id="billSummary">
            <!-- Bill summary will be populated here -->
        </div>
        <div class="form-actions">
            <button type="button" class="btn btn-secondary close-modal">Cancel</button>
            <button type="button" class="btn btn-primary" id="finalConfirmBtn">Confirm & Generate Bill</button>
        </div>
    </div>
</div>

<!-- Include Select2 CSS for the combo box with search -->
<link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet" />

<!-- Include jQuery FIRST -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

<!-- Then include Select2 JS -->
<script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>

<!-- Set global context path variable -->
<script>
    window.contextPath = "${pageContext.request.contextPath}";
</script>

<!-- Then include your custom JS -->
<script src="${pageContext.request.contextPath}/js/staff_billing.js"></script>

</body>
</html>