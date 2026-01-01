package com.example.uploadingdata;

public class CartItem {
    private String name, imageUrl, docId;
    private double price, quantity;

    public CartItem() {} // Required by Firestore

    public CartItem(String name, String imageUrl, double price, double quantity) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }
    public String getDocId() { return docId; }

    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setDocId(String docId) { this.docId = docId; }
}

