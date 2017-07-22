package org.alexgdev.bittrexgatherer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class OrderFill {
	private String OrderType;
	private double Rate;
	private double Quantity;
	private String TimeStamp;
	
	@JsonProperty("OrderType")
	public String getOrderType() {
		return OrderType;
	}
	
	@JsonProperty("OrderType")
	public void setOrderType(String orderType) {
		OrderType = orderType;
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
	
	@JsonProperty("TimeStamp")
	public String getTimeStamp() {
		return TimeStamp;
	}
	
	@JsonProperty("TimeStamp")
	public void setTimeStamp(String timeStamp) {
		TimeStamp = timeStamp;
	}
	
	
	
}
