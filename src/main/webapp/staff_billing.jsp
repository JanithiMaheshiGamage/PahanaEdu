<%@ page import="com.pahanaedu.model.*, com.pahanaedu.dao.*, java.util.*" %>
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

    // Notification handling
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
    <title>PAHANA EDU - Billing System</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        .customer-section, .items-section, .cart-section {
            margin-bottom: 20px;
            padding: 15px;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .search-results {
            max-height: 300px;
            overflow-y: auto;
            border: 1px solid #ddd;
            margin-top: 10px;
            display: none;
        }

        .search-result-item {
            padding: 10px;
            border-bottom: 1px solid #eee;
            cursor: pointer;
        }

        .search-result-item:hover {
            background-color: #f5f5f5;
        }

        .cart-table {
            width: 100%;
            border-collapse: collapse;
        }

        .cart-table th, .cart-table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        .payment-section {
            display: flex;
            gap: 20px;
            margin-top: 20px;
        }

        .payment-method {
            flex: 1;
        }

        .payment-details {
            display: none;
            margin-top: 10px;
        }

        .total-section {
            margin-top: 20px;
            text-align: right;
            font-size: 18px;
            font-weight: bold;
        }

        .action-buttons {
            margin-top: 20px;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
    </style>
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
    <!-- Staff Sidebar -->
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
            <h1 class="page-title">Billing System</h1>

            <!-- Customer Section -->
            <div class="customer-section">
                <h2>Customer Details</h2>
                <div class="form-row">
                    <div class="form-group" style="flex: 2;">
                        <label for="customerSearch">Search Customer (NIC/Name/Phone)</label>
                        <input type="text" id="customerSearch" placeholder="Enter NIC, Name or Phone">
                        <div id="customerResults" class="search-results"></div>
                    </div>
                    <div class="form-group">
                        <button type="button" class="btn btn-secondary" id="newCustomerBtn">New Customer</button>
                    </div>
                </div>

                <!-- New Customer Form (Initially Hidden) -->
                <div id="newCustomerForm" style="display: none; margin-top: 20px;">
                    <form id="addCustomerForm" method="post" action="add-customer">
                        <div class="form-row">
                            <div class="form-group">
                                <label for="newName">Full Name</label>
                                <input type="text" id="newName" name="name" required>
                            </div>
                            <div class="form-group">
                                <label for="newNic">NIC</label>
                                <input type="text" id="newNic" name="nic" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="newPhone">Phone Number</label>
                                <input type="text" id="newPhone" name="phoneNo" required>
                            </div>
                            <div class="form-group">
                                <label for="newEmail">Email</label>
                                <input type="email" id="newEmail" name="email">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group" style="flex: 2;">
                                <label for="newAddress">Address</label>
                                <textarea id="newAddress" name="address" required></textarea>
                            </div>
                        </div>
                        <div class="form-actions">
                            <button type="button" class="btn btn-secondary" id="cancelNewCustomer">Cancel</button>
                            <button type="submit" class="btn btn-primary">Add Customer</button>
                        </div>
                    </form>
                </div>

                <!-- Selected Customer Display -->
                <div id="selectedCustomer" style="display: none; margin-top: 20px;">
                    <h3>Selected Customer</h3>
                    <div class="customer-details">
                        <p><strong>Name:</strong> <span id="customerName"></span></p>
                        <p><strong>NIC:</strong> <span id="customerNic"></span></p>
                        <p><strong>Phone:</strong> <span id="customerPhone"></span></p>
                    </div>
                    <input type="hidden" id="selectedCustomerId">
                </div>
            </div>

            <!-- Items Section -->
            <div class="items-section">
                <h2>Items</h2>
                <div class="form-row">
                    <div class="form-group" style="flex: 2;">
                        <label for="itemSearch">Search Items (Code/Name)</label>
                        <input type="text" id="itemSearch" placeholder="Enter item code or name">
                        <div id="itemResults" class="search-results"></div>
                    </div>
                </div>
            </div>

            <!-- Cart Section -->
            <div class="cart-section">
                <h2>Selected Items</h2>
                <table class="cart-table">
                    <thead>
                    <tr>
                        <th>Item</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Total</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody id="cartItems">
                    <!-- Cart items will be added here dynamically -->
                    </tbody>
                </table>

                <div class="total-section">
                    <p>Subtotal: Rs. <span id="subtotal">0.00</span></p>
                </div>
            </div>

            <!-- Payment Section -->
            <div class="payment-section">
                <div class="payment-method">
                    <h3>Payment Method</h3>
                    <div class="form-group">
                        <label><input type="radio" name="paymentMethod" value="cash" checked> Cash</label>
                        <label><input type="radio" name="paymentMethod" value="card"> Card</label>
                    </div>

                    <!-- Cash Payment Details -->
                    <div id="cashDetails" class="payment-details">
                        <div class="form-group">
                            <label for="amountReceived">Amount Received (Rs.)</label>
                            <input type="number" id="amountReceived" step="0.01" min="0">
                        </div>
                        <div class="form-group">
                            <label>Change: Rs. <span id="changeAmount">0.00</span></label>
                        </div>
                    </div>

                    <!-- Card Payment Details -->
                    <div id="cardDetails" class="payment-details">
                        <div class="form-group">
                            <label for="cardNumber">Card Number</label>
                            <input type="text" id="cardNumber" placeholder="1234 5678 9012 3456">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="expiryDate">Expiry Date</label>
                                <input type="text" id="expiryDate" placeholder="MM/YY">
                            </div>
                            <div class="form-group">
                                <label for="cvv">CVV</label>
                                <input type="text" id="cvv" placeholder="123">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons">
                <button type="button" class="btn btn-secondary" id="clearCartBtn">Clear Cart</button>
                <button type="button" class="btn btn-primary" id="confirmBtn">Confirm Transaction</button>
                <button type="button" class="btn btn-success" id="printBillBtn" style="display: none;">Print Bill</button>
            </div>
        </div>
    </div>
</div>

<!-- Transaction Success Modal -->
<div id="successModal" class="modal" style="display: none;">
    <div class="modal-content">
        <span class="close-modal">&times;</span>
        <h2>Transaction Successful</h2>
        <p>Bill ID: <span id="billIdDisplay"></span></p>
        <p>Total Amount: Rs. <span id="totalAmountDisplay"></span></p>
        <div class="form-actions">
            <button type="button" class="btn btn-primary" id="downloadBillBtn">Download Bill</button>
            <button type="button" class="btn btn-secondary" id="newTransactionBtn">New Transaction</button>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Current date and time
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

        // Customer search functionality
        const customerSearch = document.getElementById('customerSearch');
        const customerResults = document.getElementById('customerResults');

        customerSearch.addEventListener('input', function() {
            const query = this.value.trim();
            if (query.length < 2) {
                customerResults.style.display = 'none';
                return;
            }

            fetch('search-customers?query=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(data => {
                    customerResults.innerHTML = '';
                    if (data.length === 0) {
                        customerResults.innerHTML = '<div class="search-result-item">No customers found</div>';
                    } else {
                        data.forEach(customer => {
                            const item = document.createElement('div');
                            item.className = 'search-result-item';
                            item.innerHTML = `
                                <strong>${customer.name}</strong><br>
                                NIC: ${customer.nic} | Phone: ${customer.phoneNo}
                                <input type="hidden" value='${JSON.stringify(customer)}'>
                            `;
                            item.addEventListener('click', function() {
                                const customerData = JSON.parse(this.querySelector('input[type="hidden"]').value);
                                displaySelectedCustomer(customerData);
                                customerResults.style.display = 'none';
                            });
                            customerResults.appendChild(item);
                        });
                    }
                    customerResults.style.display = 'block';
                });
        });

        // New customer button
        document.getElementById('newCustomerBtn').addEventListener('click', function() {
            document.getElementById('newCustomerForm').style.display = 'block';
            document.getElementById('selectedCustomer').style.display = 'none';
            document.getElementById('customerSearch').value = '';
        });

        // Cancel new customer
        document.getElementById('cancelNewCustomer').addEventListener('click', function() {
            document.getElementById('newCustomerForm').style.display = 'none';
            document.getElementById('addCustomerForm').reset();
        });

        // Item search functionality
        const itemSearch = document.getElementById('itemSearch');
        const itemResults = document.getElementById('itemResults');

        itemSearch.addEventListener('input', function() {
            const query = this.value.trim();
            if (query.length < 2) {
                itemResults.style.display = 'none';
                return;
            }

            fetch('search-items?query=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(data => {
                    itemResults.innerHTML = '';
                    if (data.length === 0) {
                        itemResults.innerHTML = '<div class="search-result-item">No items found</div>';
                    } else {
                        data.forEach(item => {
                            const itemElement = document.createElement('div');
                            itemElement.className = 'search-result-item';
                            itemElement.innerHTML = `
                                <strong>${item.name}</strong><br>
                                Price: Rs. ${item.price.toFixed(2)} | Stock: ${item.stockQty}
                                <input type="hidden" value='${JSON.stringify(item)}'>
                            `;
                            itemElement.addEventListener('click', function() {
                                const itemData = JSON.parse(this.querySelector('input[type="hidden"]').value);
                                addItemToCart(itemData);
                                itemResults.style.display = 'none';
                                itemSearch.value = '';
                            });
                            itemResults.appendChild(itemElement);
                        });
                    }
                    itemResults.style.display = 'block';
                });
        });

        // Payment method toggle
        document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
            radio.addEventListener('change', function() {
                document.querySelectorAll('.payment-details').forEach(detail => {
                    detail.style.display = 'none';
                });

                if (this.value === 'cash') {
                    document.getElementById('cashDetails').style.display = 'block';
                } else if (this.value === 'card') {
                    document.getElementById('cardDetails').style.display = 'block';
                }
            });
        });

        // Show cash details by default
        document.getElementById('cashDetails').style.display = 'block';

        // Calculate change when amount received changes
        document.getElementById('amountReceived').addEventListener('input', function() {
            const subtotal = parseFloat(document.getElementById('subtotal').textContent);
            const received = parseFloat(this.value) || 0;
            const change = received - subtotal;
            document.getElementById('changeAmount').textContent = change.toFixed(2);
        });

        // Clear cart button
        document.getElementById('clearCartBtn').addEventListener('click', function() {
            document.getElementById('cartItems').innerHTML = '';
            updateSubtotal();
        });

        // Confirm transaction button
        document.getElementById('confirmBtn').addEventListener('click', function() {
            const customerId = document.getElementById('selectedCustomerId').value;
            if (!customerId) {
                alert('Please select a customer first');
                return;
            }

            const cartItems = Array.from(document.querySelectorAll('#cartItems tr')).map(row => {
                return {
                    itemId: row.dataset.itemId,
                    quantity: row.querySelector('.item-quantity').value,
                    price: row.querySelector('.item-price').textContent.replace('Rs. ', '')
                };
            });

            if (cartItems.length === 0) {
                alert('Please add items to the cart');
                return;
            }

            const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
            const paymentDetails = {};

            if (paymentMethod === 'cash') {
                const amountReceived = parseFloat(document.getElementById('amountReceived').value);
                const subtotal = parseFloat(document.getElementById('subtotal').textContent);

                if (isNaN(amountReceived) || amountReceived < subtotal) {
                    alert('Amount received must be equal to or greater than the subtotal');
                    return;
                }

                paymentDetails.amountReceived = amountReceived;
                paymentDetails.change = amountReceived - subtotal;
            } else if (paymentMethod === 'card') {
                const cardNumber = document.getElementById('cardNumber').value;
                const expiryDate = document.getElementById('expiryDate').value;
                const cvv = document.getElementById('cvv').value;

                if (!cardNumber || !expiryDate || !cvv) {
                    alert('Please enter all card details');
                    return;
                }

                paymentDetails.cardNumber = cardNumber;
                paymentDetails.expiryDate = expiryDate;
                paymentDetails.cvv = cvv;
            }

            const transactionData = {
                customerId: customerId,
                items: cartItems,
                paymentMethod: paymentMethod,
                paymentDetails: paymentDetails,
                totalAmount: parseFloat(document.getElementById('subtotal').textContent)
            };

            // Disable button during processing
            this.disabled = true;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';

            fetch('process-transaction', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(transactionData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showSuccessModal(data.billId, data.totalAmount);
                    } else {
                        alert('Transaction failed: ' + data.message);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred during transaction processing');
                })
                .finally(() => {
                    this.disabled = false;
                    this.innerHTML = 'Confirm Transaction';
                });
        });

        // Print bill button
        document.getElementById('printBillBtn').addEventListener('click', function() {
            const billId = document.getElementById('billIdDisplay').textContent;
            window.open('generate-bill?billId=' + billId, '_blank');
        });

        // Download bill button
        document.getElementById('downloadBillBtn').addEventListener('click', function() {
            const billId = document.getElementById('billIdDisplay').textContent;
            window.location.href = 'download-bill?billId=' + billId;
        });

        // New transaction button
        document.getElementById('newTransactionBtn').addEventListener('click', function() {
            document.getElementById('successModal').style.display = 'none';
            resetTransactionForm();
        });

        // Close modal when clicking outside
        window.addEventListener('click', function(event) {
            if (event.target.classList.contains('modal')) {
                event.target.style.display = 'none';
            }
        });
    });

    // Display selected customer
    function displaySelectedCustomer(customer) {
        document.getElementById('customerName').textContent = customer.name;
        document.getElementById('customerNic').textContent = customer.nic;
        document.getElementById('customerPhone').textContent = customer.phoneNo;
        document.getElementById('selectedCustomerId').value = customer.accountNo;
        document.getElementById('selectedCustomer').style.display = 'block';
        document.getElementById('newCustomerForm').style.display = 'none';
    }

    // Add item to cart
    function addItemToCart(item) {
        // Check if item already exists in cart
        const existingRow = document.querySelector(`#cartItems tr[data-item-id="${item.itemId}"]`);

        if (existingRow) {
            // Increase quantity
            const quantityInput = existingRow.querySelector('.item-quantity');
            quantityInput.value = parseInt(quantityInput.value) + 1;
            updateItemTotal(existingRow);
        } else {
            // Add new row
            const row = document.createElement('tr');
            row.dataset.itemId = item.itemId;
            row.innerHTML = `
                <td>${item.name}</td>
                <td class="item-price">Rs. ${item.price.toFixed(2)}</td>
                <td>
                    <input type="number" class="item-quantity" value="1" min="1" max="${item.stockQty}">
                </td>
                <td class="item-total">Rs. ${item.price.toFixed(2)}</td>
                <td>
                    <button class="remove-item-btn"><i class="fas fa-trash-alt"></i></button>
                </td>
            `;

            // Add event listeners
            row.querySelector('.item-quantity').addEventListener('change', function() {
                updateItemTotal(row);
            });

            row.querySelector('.remove-item-btn').addEventListener('click', function() {
                row.remove();
                updateSubtotal();
            });

            document.getElementById('cartItems').appendChild(row);
        }

        updateSubtotal();
    }

    // Update item total when quantity changes
    function updateItemTotal(row) {
        const price = parseFloat(row.querySelector('.item-price').textContent.replace('Rs. ', ''));
        const quantity = parseInt(row.querySelector('.item-quantity').value);
        const total = price * quantity;
        row.querySelector('.item-total').textContent = 'Rs. ' + total.toFixed(2);
        updateSubtotal();
    }

    // Update subtotal
    function updateSubtotal() {
        const totals = Array.from(document.querySelectorAll('.item-total')).map(el => {
            return parseFloat(el.textContent.replace('Rs. ', ''));
        });

        const subtotal = totals.reduce((sum, total) => sum + total, 0);
        document.getElementById('subtotal').textContent = subtotal.toFixed(2);

        // Update change if cash payment
        if (document.querySelector('input[name="paymentMethod"]:checked').value === 'cash') {
            const received = parseFloat(document.getElementById('amountReceived').value) || 0;
            const change = received - subtotal;
            document.getElementById('changeAmount').textContent = change.toFixed(2);
        }
    }

    // Show success modal
    function showSuccessModal(billId, totalAmount) {
        document.getElementById('billIdDisplay').textContent = billId;
        document.getElementById('totalAmountDisplay').textContent = totalAmount.toFixed(2);
        document.getElementById('successModal').style.display = 'block';
        document.getElementById('printBillBtn').style.display = 'inline-block';
    }

    // Reset transaction form
    function resetTransactionForm() {
        document.getElementById('customerSearch').value = '';
        document.getElementById('selectedCustomer').style.display = 'none';
        document.getElementById('selectedCustomerId').value = '';
        document.getElementById('itemSearch').value = '';
        document.getElementById('cartItems').innerHTML = '';
        document.getElementById('subtotal').textContent = '0.00';
        document.querySelector('input[name="paymentMethod"][value="cash"]').checked = true;
        document.getElementById('cashDetails').style.display = 'block';
        document.getElementById('cardDetails').style.display = 'none';
        document.getElementById('amountReceived').value = '';
        document.getElementById('changeAmount').textContent = '0.00';
        document.getElementById('cardNumber').value = '';
        document.getElementById('expiryDate').value = '';
        document.getElementById('cvv').value = '';
        document.getElementById('printBillBtn').style.display = 'none';
    }
</script>
</body>
</html>