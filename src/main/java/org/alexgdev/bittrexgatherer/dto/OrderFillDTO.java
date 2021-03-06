package org.alexgdev.bittrexgatherer.dto;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class OrderFillDTO implements PriceEvent{
	private String orderType;
	private Double rate;
	private Double quantity;
	private String timeStamp;
	
	@JsonProperty("OrderType")
	public String getOrderType() {
		return orderType;
	}
	
	@JsonProperty("OrderType")
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	@JsonProperty("Rate")
	public Double getRate() {
		return rate;
	}
	
	@JsonProperty("Rate")
	public void setRate(double rate) {
		this.rate = rate;
	}
	
	@JsonProperty("Quantity")
	public Double getQuantity() {
		return quantity;
	}
	
	@JsonProperty("Quantity")
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	
	@JsonProperty("TimeStamp")
	public String getTimeStamp() {
		return timeStamp;
	}
	
	@JsonProperty("TimeStamp")
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	
}
