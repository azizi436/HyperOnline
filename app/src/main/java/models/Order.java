/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package models;

public class Order {
    public String unique_id;
    public String code;
    public String seller_name;
    public String stuffs;
    public String price;
    public int hour;
    public String method;
    public String status;
    public String description;
    public String date;
    
    public Order(String unique_id, String code, String seller_name, String stuffs, String price, int hour, String method, String status, String description, String date) {
        this.unique_id = unique_id;
        this.code = code;
        this.seller_name = seller_name;
        this.stuffs = stuffs;
        this.price = price;
        this.hour = hour;
        this.method = method;
        this.status = status;
        this.description = description;
        this.date = date;
    }
}
