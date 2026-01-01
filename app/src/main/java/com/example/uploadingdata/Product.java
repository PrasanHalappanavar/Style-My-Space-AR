package com.example.uploadingdata;

public class Product {
    private String id;
    private String imageUrl;
    private String modelUrl;
    private String name;
    private double price;
    private String type;
    private String details;
    private int count;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String id, String imageUrl, String modelUrl, String name, double price, String type) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.modelUrl = modelUrl;
        this.name = name;
        this.price = price;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getModelUrl() { return modelUrl; }
    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}

