/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

public class Product {
    public String uid;
    public String name;
    public String image;
    public String price;
    public int off;
    public int count;
    public double point;
    public int point_count;
    public String description;
    public int web3d;
    public int type;

    public Product(String uid, String name, String image, String price, int off, int count, double point, int point_count, String description, int web3d, int type) {
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.price = price;
        this.off = off;
        this.count = count;
        this.point = point;
        this.point_count = point_count;
        this.description = description;
        this.web3d = web3d;
        this.type = type;
    }
}