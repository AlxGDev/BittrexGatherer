package org.alexgdev.bittrexgatherer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderDeltaDTO {
	
	private int Type;
	private double Rate;
	private double Quantity;
	
	@JsonProperty("Type")
	public int getType() {
		return Type;
	}
	
	@JsonProperty("Type")
	public void setType(int type) {
		Type = type;
	}
	
	@JsonProperty("Rate")
	public double getRate() {
		return Rate;
	}
	
	@JsonProperty("Rate")
	public void setRate(double rate) {
		Rate = rate;
	}
	
	@JsonProperty("Quantity")
	public double getQuantity() {
		return Quantity;
	}
	
	@JsonProperty("Quantity")
	public void setQuantity(double quantity) {
		Quantity = quantity;
	}
	
	
}
