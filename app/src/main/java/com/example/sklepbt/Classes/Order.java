package com.example.sklepbt.Classes;

import java.util.List;

public class Order {
    private int id;
    private int userId;
    private List<Product> products;
    private List<Integer> quantities;
    private String orderDate;
    private double totalPrice;

    public Order(int id, int userId, List<Product> products, List<Integer> quantities, String orderDate) {
        this.id = id;
        this.userId = userId;
        this.products = products;
        this.quantities = quantities;
        this.orderDate = orderDate;
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        totalPrice = 0;
        for (int i = 0; i < products.size(); i++) {
            totalPrice += products.get(i).getPrice() * quantities.get(i);
        }
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
