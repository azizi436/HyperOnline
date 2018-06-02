package ir.hatamiarash.hyperonline.models;

public class Transaction {
	public String date;
	public String price;
	public String card;
	public String description;
	
	public Transaction(String date, String price, String card, String description) {
		this.date = date;
		this.price = price;
		this.card = card;
		this.description = description;
	}
}
