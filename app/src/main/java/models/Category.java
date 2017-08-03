/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package models;

public class Category {
    public String name;
    public String info;
    public int image;
    
    public Category(String name, String info, int image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }
}