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
                <div class="customer-flow">
                    <!-- Step 1: Search or Add Customer -->
                    <div class="customer-step active" id="step-search">
                        <h4>Search Customer</h4>

                        <div class="search-container">
                            <div class="search-bar">
                                <input type="text" id="customerSearch" placeholder="Enter NIC, Name or Phone" autocomplete="off">
                            </div>
                            <div class="search-actions">
                                <button type="button" class="btn btn-secondary" id="newCustomerBtn">
                                    <i class="fas fa-user-plus"></i> Add New Customer
                                </button>
                            </div>

                            <div id="customerResults" class="search-results">
                                <div class="search-placeholder">
                                    <i class="fas fa-search"></i>
                                    <p>Search for customers by NIC, name or phone number</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Step 2: Confirm Selected Customer -->
                    <div class="customer-step" id="step-confirm">

                        <div class="customer-card selected">
                            <div class="customer-header">
                                <div class="customer-avatar">
                                    <i class="fas fa-user"></i>
                                </div>
                                <div class="customer-info">
                                    <h3 id="customerNameDisplay">No customer selected</h3>
                                    <div class="customer-meta">
                                        <span><i class="fas fa-id-card"></i> NIC: <span id="customerNicDisplay">-</span></span>
                                        <span><i class="fas fa-phone"></i> Phone: <span id="customerPhoneDisplay">-</span></span>
                                    </div>
                                </div>
                            </div>

                            <div class="customer-details">
                                <div class="detail-row">
                                    <span class="detail-label">Email:</span>
                                    <span id="customerEmailDisplay" class="detail-value">-</span>
                                </div>
                            </div>

                            <div class="customer-actions">
                                <button type="button" class="btn btn-danger" id="changeCustomerBtn">
                                    <i class="fas fa-times"></i> Change Customer
                                </button>
                                <button type="button" class="btn btn-success" id="confirmCustomerBtn">
                                    <i class="fas fa-check"></i> Confirm & Continue
                                </button>
                            </div>
                        </div>

                        <input type="hidden" id="selectedCustomerId">
                    </div>
                </div>

                <!-- New Customer Form (Modal) -->
                <div id="newCustomerModal" class="modal">
                    <div class="modal-content">
                        <span class="close-modal">&times;</span>
                        <h2>Add New Customer</h2>

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
                                <div class="form-group full-width">
                                    <label for="newAddress">Address</label>
                                    <textarea id="newAddress" name="address" required></textarea>
                                </div>
                            </div>
                            <div class="form-actions">
                                <button type="button" class="btn btn-secondary" id="cancelNewCustomer">Cancel</button>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save"></i> Save Customer
                                </button>
                            </div>
                        </form>
                    </div>
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


        // Customer Section Functionality
        let selectedCustomer = null;

// Search functionality with debounce
        customerSearch.addEventListener('input', debounce(function() {
            const query = this.value.trim();

            if (query.length < 2) {
                customerResults.innerHTML = `
            <div class="search-placeholder">
                <i class="fas fa-search"></i>
                <p>Search for customers by NIC, name or phone number</p>
            </div>
        `;
                return;
            }

            // Show loading state
            customerResults.innerHTML = `
        <div class="search-placeholder">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Searching customers...</p>
        </div>
    `;

            fetch('search-customers?query=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(data => {
                    if (data.length === 0) {
                        customerResults.innerHTML = `
                    <div class="search-placeholder">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>No customers found</p>
                    </div>
                `;
                    } else {
                        customerResults.innerHTML = '';
                        data.forEach(customer => {
                            const item = document.createElement('div');
                            item.className = 'search-result-item';
                            item.innerHTML = `
                        <h4>${customer.name}</h4>
                        <p><i class="fas fa-id-card"></i> ${customer.nic} | <i class="fas fa-phone"></i> ${customer.phoneNo}</p>
                        <input type="hidden" value='${JSON.stringify(customer)}'>
                    `;
                            item.addEventListener('click', function() {
                                selectedCustomer = JSON.parse(this.querySelector('input[type="hidden"]').value);

                                // Highlight selected item
                                document.querySelectorAll('.search-result-item').forEach(el => {
                                    el.classList.remove('selected');
                                });
                                this.classList.add('selected');

                                // Show customer details
                                displayCustomerDetails(selectedCustomer);
                            });
                            customerResults.appendChild(item);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    customerResults.innerHTML = `
                <div class="search-placeholder error">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>Error loading customers</p>
                </div>
            `;
                });
        }, 300));

// Display customer details
        function displayCustomerDetails(customer) {
            document.getElementById('customerNameDisplay').textContent = customer.name;
            document.getElementById('customerNicDisplay').textContent = customer.nic;
            document.getElementById('customerPhoneDisplay').textContent = customer.phoneNo;
            document.getElementById('customerEmailDisplay').textContent = customer.email || 'Not provided';
            document.getElementById('selectedCustomerId').value = customer.accountNo;

            // Move to confirmation step
            document.getElementById('step-search').classList.remove('active');
            document.getElementById('step-confirm').classList.add('active');
        }

// Confirm customer button
        document.getElementById('confirmCustomerBtn').addEventListener('click', function() {
            // Here you would typically enable the items section
            // For now we'll just show a success message
            alert('Customer confirmed! You can now add items to the bill.');
        });

// Change customer button
        document.getElementById('changeCustomerBtn').addEventListener('click', function() {
            selectedCustomer = null;
            document.getElementById('step-confirm').classList.remove('active');
            document.getElementById('step-search').classList.add('active');
            document.getElementById('customerSearch').value = '';
            document.getElementById('customerSearch').focus();
        });

// New customer button
        document.getElementById('newCustomerBtn').addEventListener('click', function() {
            document.getElementById('newCustomerModal').style.display = 'block';
        });

// Close modal
        document.querySelector('.close-modal').addEventListener('click', function() {
            document.getElementById('newCustomerModal').style.display = 'none';
        });

// Cancel new customer
        document.getElementById('cancelNewCustomer').addEventListener('click', function() {
            document.getElementById('newCustomerModal').style.display = 'none';
            document.getElementById('addCustomerForm').reset();
        });

// Handle new customer form submission
        document.getElementById('addCustomerForm').addEventListener('submit', function(e) {
            e.preventDefault();

            // Disable button during submission
            const submitBtn = this.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

            // Simulate form submission
            setTimeout(() => {
                // In a real app, this would be an AJAX call
                alert('Customer added successfully!');

                // Close modal and reset form
                document.getElementById('newCustomerModal').style.display = 'none';
                this.reset();
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-save"></i> Save Customer';

                // Set the newly added customer as selected
                // In a real app, you would get this data from the server response
                const newCustomer = {
                    name: document.getElementById('newName').value,
                    nic: document.getElementById('newNic').value,
                    phoneNo: document.getElementById('newPhone').value,
                    email: document.getElementById('newEmail').value,
                    accountNo: 'new-customer-id' // This would come from server
                };

                selectedCustomer = newCustomer;
                displayCustomerDetails(newCustomer);
            }, 1000);
        });

// Debounce function
        function debounce(func, wait) {
            let timeout;
            return function() {
                const context = this, args = arguments;
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    func.apply(context, args);
                }, wait);
            };
        }

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

        // Display selected customer
        function displaySelectedCustomer(customer) {
            document.getElementById('customerNameDisplay').textContent = customer.name;
            document.getElementById('customerNicDisplay').textContent = customer.nic;
            document.getElementById('customerPhoneDisplay').textContent = customer.phoneNo;
            document.getElementById('customerEmailDisplay').textContent = customer.email || 'N/A';
            document.getElementById('selectedCustomerId').value = customer.accountNo;

            // Change the Select button to Remove
            const selectBtn = document.getElementById('selectCustomerBtn');
            selectBtn.textContent = 'Remove';
            selectBtn.classList.remove('btn-secondary');
            selectBtn.classList.add('btn-danger');
            selectBtn.onclick = function() {
                removeSelectedCustomer();
            };
        }

        // Remove selected customer
        function removeSelectedCustomer() {
            document.getElementById('customerNameDisplay').textContent = 'Not selected';
            document.getElementById('customerNicDisplay').textContent = '-';
            document.getElementById('customerPhoneDisplay').textContent = '-';
            document.getElementById('customerEmailDisplay').textContent = '-';
            document.getElementById('selectedCustomerId').value = '';

            // Change the Remove button back to Select
            const selectBtn = document.getElementById('selectCustomerBtn');
            selectBtn.textContent = 'Select';
            selectBtn.classList.remove('btn-danger');
            selectBtn.classList.add('btn-secondary');
            selectBtn.onclick = function() {
                if (selectedCustomer) {
                    displaySelectedCustomer(selectedCustomer);
                    customerResults.style.display = 'none';
                } else {
                    alert('Please select a customer from the search results first');
                }
            };

            // Clear selected customer
            selectedCustomer = null;
            if (selectedCustomerElement) {
                selectedCustomerElement.classList.remove('selected');
                selectedCustomerElement = null;
            }
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

            // Reset customer selection
            removeSelectedCustomer();
        }
    });
</script>
</body>
</html>