/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package models;

public class Product {
    public String uid;
    public String name;
    public int image;
    public String price;
    public int off;
    public int count;
    public double point;
    public int point_count;
    public String description;
    public int type;
    
    public Product() {
    }
    
    public Product(String uid, String name, int image, String price, int off, int count, double point, int point_count, String description) {
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.price = price;
        this.off = off;
        this.count = count;
        this.point = point;
        this.point_count = point_count;
        this.description = description;
        this.type = 0;
    }
}