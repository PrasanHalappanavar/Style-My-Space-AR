package com.example.uploadingdata;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private String shippingAddress;
    private String customerName;
    private String customerPhone;
    private Date orderDate;
    private String orderStatus;

    // Inner class for order items
    public static class OrderItem {
        private String productName;
        private String imageUrl;
        private double price;
        private int quantity;
        private double totalPrice;

        public OrderItem() {} // Required for Firestore

        public OrderItem(String productName, String imageUrl, double price, int quantity) {
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.price = price;
            this.quantity = quantity;
            this.totalPrice = price * quantity;
        }

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    }

    public Order() {} // Required for Firestore

    public Order(String orderId, String userId, List<OrderItem> items, double totalAmount,
                 String shippingAddress, String customerName, String customerPhone) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.orderDate = new Date();
        this.orderStatus = "Placed";
        this.paymentStatus = "Pending";
    }

    // All getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}