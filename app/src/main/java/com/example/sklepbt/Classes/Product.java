package com.example.sklepbt.Classes;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String description;
    private String imagePath;
    private int price;
    private int id;

    public Product(int id, String name, String description, String imagePath, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.price = price;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getPrice() {
        return price;
    }
}
