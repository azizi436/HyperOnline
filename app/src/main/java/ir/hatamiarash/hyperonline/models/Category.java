/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.models;

public class Category {
	public String unique_id;
	public String name;
	public String image;
	public double point;
	public int point_count;
	public int off;
	public int level;
	
	public Category(String unique_id, String name, String image, double point, int point_count, int off, int level) {
		this.unique_id = unique_id;
		this.name = name;
		this.point = point;
		this.point_count = point_count;
		this.off = off;
		this.image = image;
		this.level = level;
	}
}