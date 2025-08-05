// Ensure contextPath is available
if (!window.contextPath) {
    console.warn('contextPath not defined, defaulting to empty string');
    window.contextPath = '';
}

document.addEventListener('DOMContentLoaded', function() {
    // Initialize Select2 for item search
    $('#itemSearch').select2({
        placeholder: "Search by item ID or name",
        minimumInputLength: 1,
        ajax: {
            url: window.contextPath + '/search-items',
            dataType: 'json',
            delay: 250,
            data: function(params) {
                return {
                    term: params.term
                };
            },
            processResults: function(data) {
                return {
                    results: data.items.map(item => ({
                        id: item.itemId,
                        text: `${item.name} (${item.categoryName}) - LKR ${item.price.toFixed(2)}`,
                        item: item
                    }))
                };
            },
            cache: true
        }
    });

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

    // Payment method toggle
    const paymentMethods = document.querySelectorAll('input[name="paymentMethod"]');
    const cashPaymentDetails = document.getElementById('cashPaymentDetails');
    const cardPaymentDetails = document.getElementById('cardPaymentDetails');

    paymentMethods.forEach(method => {
        method.addEventListener('change', function() {
            if (this.value === 'cash') {
                cashPaymentDetails.style.display = 'block';
                cardPaymentDetails.style.display = 'none';
            } else {
                cashPaymentDetails.style.display = 'none';
                cardPaymentDetails.style.display = 'block';
            }
        });
    });

    // Calculate change when amount received changes
    const amountReceivedInput = document.getElementById('amountReceived');
    const changeAmountDiv = document.getElementById('changeAmount');

    amountReceivedInput.addEventListener('input', function() {
        const total = parseFloat(document.getElementById('cartTotal').textContent.replace('LKR', '').trim());
        const received = parseFloat(this.value) || 0;
        const change = received - total;
        changeAmountDiv.textContent = change >= 0 ? change.toFixed(2) : '0.00';
    });

    // Check customer by phone number
    const checkCustomerBtn = document.getElementById('checkCustomerBtn');
    const customerPhoneInput = document.getElementById('customerPhone');
    const customerSearchResults = document.getElementById('customerSearchResults');

    checkCustomerBtn.addEventListener('click', function() {
        const phoneNumber = customerPhoneInput.value.trim();
        if (!phoneNumber) {
            alert('Please enter a phone number');
            return;
        }

        fetch(window.contextPath + '/check-customer?phone=' + encodeURIComponent(phoneNumber))
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    customerSearchResults.innerHTML = `<div class="search-placeholder">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>${data.error}</p>
                    </div>`;
                    customerSearchResults.style.display = 'block';
                } else if (data.customers && data.customers.length > 0) {
                    let html = '';
                    data.customers.forEach(customer => {
                        html += `<div class="search-result-item" data-customer-id="${customer.accountNo}">
                            <h4>${customer.name}</h4>
                            <p><i class="fas fa-phone"></i> ${customer.phoneNo} | <i class="fas fa-id-card"></i> ${customer.nic || 'N/A'}</p>
                        </div>`;
                    });
                    customerSearchResults.innerHTML = html;
                    customerSearchResults.style.display = 'block';

                    // Add click event to search results
                    document.querySelectorAll('.search-result-item').forEach(item => {
                        item.addEventListener('click', function() {
                            const customerId = this.getAttribute('data-customer-id');
                            selectCustomer(data.customers.find(c => c.accountNo === customerId));
                        });
                    });
                } else {
                    customerSearchResults.innerHTML = `<div class="search-placeholder">
                        <i class="fas fa-search"></i>
                        <p>No customer found with this phone number</p>
                    </div>`;
                    customerSearchResults.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                customerSearchResults.innerHTML = `<div class="search-placeholder">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Error searching for customer</p>
                </div>`;
                customerSearchResults.style.display = 'block';
            });
    });

    // Select customer function
    function selectCustomer(customer) {
        fetch(window.contextPath + '/select-customer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(customer)
        })
            .then(response => {
                if (response.ok) {
                    location.reload(); // Refresh to show selected customer
                }
            })
            .catch(error => console.error('Error:', error));
    }

    // Clear selected customer
    const clearCustomerBtn = document.getElementById('clearCustomerBtn');
    if (clearCustomerBtn) {
        clearCustomerBtn.addEventListener('click', function() {
            fetch(window.contextPath + '/clear-customer', {
                method: 'POST'
            })
                .then(response => {
                    if (response.ok) {
                        location.reload();
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    }

    // Add item to cart
    const addItemBtn = document.getElementById('addItemBtn');
    const itemQuantityInput = document.getElementById('itemQuantity');
    const cartItemsTable = document.getElementById('cartItems');

    addItemBtn.addEventListener('click', function() {
        const selectedItem = $('#itemSearch').select2('data')[0];
        if (!selectedItem) {
            alert('Please select an item');
            return;
        }

        const quantity = parseInt(itemQuantityInput.value) || 1;
        if (quantity <= 0) {
            alert('Quantity must be at least 1');
            return;
        }

        const item = selectedItem.item;
        const subtotal = item.price * quantity;

        // Check if item already exists in cart
        const existingRow = document.querySelector(`#cartItems tr[data-item-id="${item.itemId}"]`);
        if (existingRow) {
            const existingQty = parseInt(existingRow.querySelector('.item-quantity').textContent);
            const newQty = existingQty + quantity;
            existingRow.querySelector('.item-quantity').textContent = newQty;
            existingRow.querySelector('.item-subtotal').textContent = `LKR ${(item.price * newQty).toFixed(2)}`;
        } else {
            const row = document.createElement('tr');
            row.setAttribute('data-item-id', item.itemId);
            row.innerHTML = `
                <td>${item.itemId}</td>
                <td>${item.name}</td>
                <td>${item.categoryName}</td>
                <td>LKR ${item.price.toFixed(2)}</td>
                <td class="item-quantity">${quantity}</td>
                <td class="item-subtotal">LKR ${subtotal.toFixed(2)}</td>
                <td><button class="btn btn-secondary remove-item-btn" data-item-id="${item.itemId}">
                    <i class="fas fa-trash"></i> Remove
                </button></td>
            `;
            cartItemsTable.appendChild(row);
        }

        // Add event listener to remove button
        row.querySelector('.remove-item-btn').addEventListener('click', function() {
            removeItemFromCart(item.itemId);
        });

        updateCartTotal();
        $('#itemSearch').val(null).trigger('change');
        itemQuantityInput.value = 1;
    });

    // Remove item from cart
    function removeItemFromCart(itemId) {
        const row = document.querySelector(`#cartItems tr[data-item-id="${itemId}"]`);
        if (row) {
            row.remove();
            updateCartTotal();
        }
    }

    // Update cart total
    function updateCartTotal() {
        let total = 0;
        document.querySelectorAll('.item-subtotal').forEach(cell => {
            total += parseFloat(cell.textContent.replace('LKR', '').trim());
        });

        document.getElementById('cartTotal').textContent = `LKR ${total.toFixed(2)}`;

        // Update change calculation if payment method is cash
        if (document.querySelector('input[name="paymentMethod"]:checked').value === 'cash') {
            const received = parseFloat(amountReceivedInput.value) || 0;
            const change = received - total;
            changeAmountDiv.textContent = change >= 0 ? change.toFixed(2) : '0.00';
        }
    }

    // Clear cart
    const clearCartBtn = document.getElementById('clearCartBtn');
    clearCartBtn.addEventListener('click', function() {
        if (confirm('Are you sure you want to clear all items from the cart?')) {
            cartItemsTable.innerHTML = '<tr><td colspan="7" style="text-align: center;">No items added yet</td></tr>';
            updateCartTotal();
        }
    });

    // Add new customer modal
    const addCustomerModal = document.getElementById('addCustomerModal');
    const addCustomerBtn = document.getElementById('addCustomerBtn');
    const closeModalButtons = document.querySelectorAll('.close-modal');
    const customerForm = document.getElementById('customerForm');

    addCustomerBtn.addEventListener('click', function() {
        console.log("Add Customer button clicked");
        addCustomerModal.style.display = 'block';
        document.body.classList.add('body-modal-open');
    });

    closeModalButtons.forEach(button => {
        button.addEventListener('click', function() {
            addCustomerModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        });
    });

    // Customer form submission
    customerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // Show loading state
        const saveBtn = this.querySelector('button[type="submit"]');
        saveBtn.disabled = true;
        document.getElementById('saveCustomerText').style.display = 'none';
        document.getElementById('saveCustomerLoading').style.display = 'inline-block';

        try {
            const customerData = {
                name: document.getElementById('newCustomerName').value.trim(),
                phoneNo: document.getElementById('newCustomerPhone').value.trim(),
                nic: document.getElementById('newCustomerNIC').value.trim(),
                email: document.getElementById('newCustomerEmail').value.trim(),
                address: document.getElementById('newCustomerAddress').value.trim()
            };

            // Validate required fields
            if (!customerData.name || !customerData.phoneNo) {
                throw new Error("Name and Phone Number are required");
            }

            const response = await fetch(window.contextPath + '/add-customer', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(customerData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to add customer');
            }

            const data = await response.json();

            if (data.success) {
                // Success: Close modal and refresh customer list
                addCustomerModal.style.display = 'none';
                document.body.classList.remove('body-modal-open');
                selectCustomer(data.customer);
            } else {
                throw new Error(data.message || 'Failed to add customer');
            }
        } catch (error) {
            console.error('Error:', error);
            alert(error.message || 'Error adding customer');
        } finally {
            // Reset button state
            saveBtn.disabled = false;
            document.getElementById('saveCustomerText').style.display = 'inline-block';
            document.getElementById('saveCustomerLoading').style.display = 'none';
        }
    });

    // Confirm bill
    const confirmBillBtn = document.getElementById('confirmBillBtn');
    const billConfirmationModal = document.getElementById('billConfirmationModal');
    const billSummaryDiv = document.getElementById('billSummary');

    confirmBillBtn.addEventListener('click', function() {
        // Validate customer is selected
        if (!document.getElementById('selectedCustomerCard')) {
            alert('Please select a customer first');
            return;
        }

        // Validate items are in cart
        if (cartItemsTable.querySelectorAll('tr').length <= 1) { // 1 for the "no items" row
            alert('Please add at least one item to the cart');
            return;
        }

        // Validate payment details
        const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
        if (paymentMethod === 'cash') {
            const amountReceived = parseFloat(amountReceivedInput.value) || 0;
            const total = parseFloat(document.getElementById('cartTotal').textContent.replace('LKR', '').trim());
            if (amountReceived < total) {
                alert('Amount received must be at least equal to the total amount');
                return;
            }
        } else if (paymentMethod === 'card') {
            const cardNumber = document.getElementById('cardNumber').value.trim();
            const cardHolder = document.getElementById('cardHolder').value.trim();
            const expiryDate = document.getElementById('expiryDate').value.trim();
            const cvv = document.getElementById('cvv').value.trim();

            if (!cardNumber || !cardHolder || !expiryDate || !cvv) {
                alert('Please fill all card details');
                return;
            }
        }

        // Prepare bill summary
        let summaryHTML = `
            <div class="bill-summary-section">
                <h4>Customer Details</h4>
                <p><strong>Name:</strong> ${document.querySelector('#selectedCustomerCard .customer-info h3').textContent}</p>
                <p><strong>Phone:</strong> ${document.querySelector('#selectedCustomerCard .customer-meta span:nth-child(2)').textContent.replace('Phone: ', '')}</p>
            </div>
            <div class="bill-summary-section">
                <h4>Items</h4>
                <table class="summary-table">
                    <thead>
                        <tr>
                            <th>Item</th>
                            <th>Qty</th>
                            <th>Price</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>`;

        document.querySelectorAll('#cartItems tr[data-item-id]').forEach(row => {
            summaryHTML += `
                <tr>
                    <td>${row.cells[1].textContent}</td>
                    <td>${row.cells[4].textContent}</td>
                    <td>${row.cells[3].textContent}</td>
                    <td>${row.cells[5].textContent}</td>
                </tr>`;
        });

        summaryHTML += `
                    </tbody>
                </table>
            </div>
            <div class="bill-summary-section">
                <h4>Payment</h4>
                <p><strong>Method:</strong> ${paymentMethod.toUpperCase()}</p>`;

        if (paymentMethod === 'cash') {
            summaryHTML += `
                <p><strong>Amount Received:</strong> LKR ${parseFloat(amountReceivedInput.value).toFixed(2)}</p>
                <p><strong>Change:</strong> LKR ${changeAmountDiv.textContent}</p>`;
        } else {
            summaryHTML += `
                <p><strong>Card:</strong> **** **** **** ${document.getElementById('cardNumber').value.slice(-4)}</p>`;
        }

        summaryHTML += `
                <p class="total-amount"><strong>Total Amount:</strong> ${document.getElementById('cartTotal').textContent}</p>
            </div>`;

        billSummaryDiv.innerHTML = summaryHTML;
        billConfirmationModal.style.display = 'block';
        document.body.classList.add('body-modal-open');
    });

    // Final confirmation and bill generation
    const finalConfirmBtn = document.getElementById('finalConfirmBtn');
    finalConfirmBtn.addEventListener('click', function() {
        const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
        const paymentDetails = paymentMethod === 'cash' ? {
            amountReceived: parseFloat(amountReceivedInput.value),
            change: parseFloat(changeAmountDiv.textContent)
        } : {
            cardNumber: document.getElementById('cardNumber').value,
            cardHolder: document.getElementById('cardHolder').value,
            expiryDate: document.getElementById('expiryDate').value,
            cvv: document.getElementById('cvv').value
        };

        const items = [];
        document.querySelectorAll('#cartItems tr[data-item-id]').forEach(row => {
            items.push({
                itemId: row.getAttribute('data-item-id'),
                name: row.cells[1].textContent,
                price: parseFloat(row.cells[3].textContent.replace('LKR', '')),
                quantity: parseInt(row.cells[4].textContent),
                subtotal: parseFloat(row.cells[5].textContent.replace('LKR', ''))
            });
        });

        const billData = {
            customerAccountNo: document.querySelector('#selectedCustomerCard .detail-value').textContent.trim(),
            items: items,
            paymentMethod: paymentMethod,
            paymentDetails: paymentDetails,
            totalAmount: parseFloat(document.getElementById('cartTotal').textContent.replace('LKR', ''))
        };

        fetch(window.contextPath + '/generate-bill', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(billData)
        })
            .then(response => {
                if (response.ok) {
                    return response.blob();
                }
                throw new Error('Bill generation failed');
            })
            .then(blob => {
                // Create download link for the PDF bill
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                const now = new Date();
                const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
                a.download = `bill_${dateStr}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);

                // Close modal and reset form
                billConfirmationModal.style.display = 'none';
                document.body.classList.remove('body-modal-open');

                // Redirect or show success message
                alert('Bill generated successfully!');
                window.location.reload();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error generating bill');
            });
    });

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === addCustomerModal) {
            addCustomerModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        }
        if (event.target === billConfirmationModal) {
            billConfirmationModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        }
    });
});