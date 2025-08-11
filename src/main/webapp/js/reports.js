function initializeCharts() {
    // Common chart options
    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        let label = context.dataset.label || '';
                        if (label) {
                            label += ': ';
                        }
                        if (context.parsed.y !== null) {
                            if (context.dataset.label === 'Quantity Sold') {
                                label += context.parsed.y;
                            } else {
                                label += 'LKR ' + context.parsed.y.toFixed(2);
                            }
                        }
                        return label;
                    }
                }
            }
        }
    };

    // Initialize charts based on report type
    switch(window.reportData.reportType) {
        case 'sales':
            initializeSalesCharts(chartOptions);
            break;
        case 'inventory':
            initializeInventoryCharts(chartOptions);
            break;
        case 'customer':
            initializeCustomerCharts(chartOptions);
            break;
        case 'popular':
            initializePopularItemsCharts(chartOptions);
            break;
    }

    // Export PDF button
    document.getElementById('exportPdf').addEventListener('click', function() {
        const params = new URLSearchParams();
        params.append('reportType', window.reportData.reportType);
        params.append('startDate', window.reportData.startDate);
        params.append('endDate', window.reportData.endDate);

        window.open(`${window.reportData.contextPath}/export/report?${params.toString()}`, '_blank');
    });
}

function initializeSalesCharts(options) {
    fetch(`${window.reportData.contextPath}/api/reports/sales?startDate=${window.reportData.startDate}&endDate=${window.reportData.endDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Daily Sales Chart
            const salesCtx = document.getElementById('salesChart');
            if (salesCtx) {
                new Chart(salesCtx, {
                    type: 'line',
                    data: {
                        labels: data.dailySales.labels,
                        datasets: [{
                            label: 'Daily Sales',
                            data: data.dailySales.values,
                            borderColor: 'rgb(75, 192, 192)',
                            backgroundColor: 'rgba(75, 192, 192, 0.2)',
                            tension: 0.1,
                            fill: true
                        }]
                    },
                    options: options
                });
            }

            // Payment Method Chart
            const paymentCtx = document.getElementById('paymentMethodChart');
            if (paymentCtx) {
                new Chart(paymentCtx, {
                    type: 'doughnut',
                    data: {
                        labels: data.paymentMethods.labels,
                        datasets: [{
                            label: 'Payment Methods',
                            data: data.paymentMethods.values,
                            backgroundColor: [
                                'rgba(54, 162, 235, 0.7)',
                                'rgba(255, 99, 132, 0.7)'
                            ],
                            borderColor: [
                                'rgba(54, 162, 235, 1)',
                                'rgba(255, 99, 132, 1)'
                            ],
                            borderWidth: 1
                        }]
                    },
                    options: options
                });
            }
        })
        .catch(error => {
            console.error('Error loading sales data:', error);
            // Optional: Display error message to user
        });
}

function initializeInventoryCharts(options) {
    // Fetch inventory data from backend
    fetch(`${window.reportData.contextPath}/api/reports/inventory`)
        .then(response => response.json())
        .then(data => {
            // Inventory by Category Chart
            const inventoryCtx = document.getElementById('inventoryChart').getContext('2d');
            new Chart(inventoryCtx, {
                type: 'bar',
                data: {
                    labels: data.categories,
                    datasets: [{
                        label: 'Items in Stock',
                        data: data.stockLevels,
                        backgroundColor: 'rgba(54, 162, 235, 0.7)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    ...options,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error loading inventory data:', error);
        });
}

function initializeCustomerCharts(options) {
    // Fetch customer transactions data from backend
    fetch(`${window.reportData.contextPath}/api/reports/customers?startDate=${window.reportData.startDate}&endDate=${window.reportData.endDate}`)
        .then(response => response.json())
        .then(data => {
            // Customer Transactions Chart
            const customerCtx = document.getElementById('customerChart').getContext('2d');
            new Chart(customerCtx, {
                type: 'bar',
                data: {
                    labels: data.customerActivity.labels,
                    datasets: [
                        {
                            label: 'Transactions',
                            data: data.customerActivity.transactions,
                            backgroundColor: 'rgba(75, 192, 192, 0.7)',
                            borderColor: 'rgba(75, 192, 192, 1)',
                            borderWidth: 1
                        },
                        {
                            label: 'Revenue',
                            data: data.customerActivity.revenue,
                            backgroundColor: 'rgba(153, 102, 255, 0.7)',
                            borderColor: 'rgba(153, 102, 255, 1)',
                            borderWidth: 1,
                            yAxisID: 'y1'
                        }
                    ]
                },
                options: {
                    ...options,
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Number of Transactions'
                            }
                        },
                        y1: {
                            beginAtZero: true,
                            position: 'right',
                            grid: {
                                drawOnChartArea: false
                            },
                            title: {
                                display: true,
                                text: 'Revenue (LKR)'
                            },
                            ticks: {
                                callback: function(value) {
                                    return 'LKR ' + value.toFixed(2);
                                }
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error loading customer data:', error);
        });
}

function initializePopularItemsCharts(options) {
    // Fetch popular items data from backend
    fetch(`${window.reportData.contextPath}/api/reports/popular-items?startDate=${window.reportData.startDate}&endDate=${window.reportData.endDate}`)
        .then(response => response.json())
        .then(data => {
            // Popular Items Chart
            const popularCtx = document.getElementById('popularItemsChart').getContext('2d');

            // Sort items by quantity sold (descending)
            const sortedItems = [...data.items].sort((a, b) => b.quantity - a.quantity);
            const topItems = sortedItems.slice(0, 10);

            new Chart(popularCtx, {
                type: 'bar',
                data: {
                    labels: topItems.map(item => item.name),
                    datasets: [
                        {
                            label: 'Quantity Sold',
                            data: topItems.map(item => item.quantity),
                            backgroundColor: 'rgba(255, 159, 64, 0.7)',
                            borderColor: 'rgba(255, 159, 64, 1)',
                            borderWidth: 1
                        },
                        {
                            label: 'Revenue',
                            data: topItems.map(item => item.total),
                            backgroundColor: 'rgba(54, 162, 235, 0.7)',
                            borderColor: 'rgba(54, 162, 235, 1)',
                            borderWidth: 1,
                            yAxisID: 'y1'
                        }
                    ]
                },
                options: {
                    ...options,
                    indexAxis: 'y',
                    scales: {
                        x: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Quantity Sold'
                            }
                        },
                        x1: {
                            beginAtZero: true,
                            position: 'top',
                            grid: {
                                drawOnChartArea: false
                            },
                            title: {
                                display: true,
                                text: 'Revenue (LKR)'
                            },
                            ticks: {
                                callback: function(value) {
                                    return 'LKR ' + value.toFixed(2);
                                }
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error loading popular items data:', error);
        });
}

// Notification handling
document.querySelectorAll('.close-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        this.closest('.notification').remove();
    });
});

// Auto-close notifications after 5 seconds
setTimeout(() => {
    document.querySelectorAll('.notification').forEach(notification => {
        notification.remove();
    });
}, 5000);

// Initialize charts when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeCharts();
});