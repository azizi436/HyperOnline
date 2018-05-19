/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.models;

public class Product {
	public String unique_id;
	public String name;
	public String image;
	public String price;
	public int off;
	public int count;
	public double point;
	public int point_count;
	public String description;
	
	public Product(String unique_id, String name, String image, String price, int off, int count, double point, int point_count, String description) {
		this.unique_id = unique_id;
		this.name = name;
		this.image = image;
		this.price = price;
		this.off = off;
		this.count = count;
		this.point = point;
		this.point_count = point_count;
		this.description = description;
	}
}