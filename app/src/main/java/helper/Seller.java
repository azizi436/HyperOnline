/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

public class Seller {
    public String uid;
    public String aid;
    public String name;
    public String image;
    public double point;
    public int point_count;
    public String address;
    public int open_hour;
    public int close_hour;
    public int off;
    public int type;
    public int closed;
    public int confirmed;
    public String phone;
    public String country;
    public String province;
    public String city;
    public String video;
    public String description;
    public String location_x;
    public String location_y;

    public Seller(String uid,String aid, String name, String image, double point, int point_count, String address, int open_hour, int close_hour, int off, int type, int closed, int confirmed, String phone, String country, String province, String city, String video, String description, String location_x, String location_y) {
        this.uid = uid;
        this.aid = aid;
        this.name = name;
        this.image = image;
        this.point = point;
        this.point_count = point_count;
        this.address = address;
        this.open_hour = open_hour;
        this.close_hour = close_hour;
        this.off = off;
        this.type = type;
        this.closed = closed;
        this.confirmed = confirmed;
        this.phone = phone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.video = video;
        this.description = description;
        this.location_x = location_x;
        this.location_y = location_y;
    }
}