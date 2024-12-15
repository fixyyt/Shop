package com.example.sklepbt.Classes;

import java.util.List;

public class Order {
    private int id;
    private int userId;
    private List<Product> products;
    private List<Integer> quantities;
    private String orderDate;

    public Order(int id, int userId, List<Product> products, List<Integer> quantities, String orderDate) {
        this.id = id;
        this.userId = userId;
        this.products = products;
        this.quantities = quantities;
        this.orderDate = orderDate;
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
}
