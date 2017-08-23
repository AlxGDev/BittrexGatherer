package org.alexgdev.bittrexgatherer.dto;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderDeltaDTO implements PriceEvent{
	
	private Integer Type;
	private Double Rate;
	private Double Quantity;
	
	@JsonProperty("Type")
	public int getType() {
		return Type;
	}
	
	@JsonProperty("Type")
	public void setType(int type) {
		Type = type;
	}
	
	@JsonProperty("Rate")
	public Double getRate() {
		return Rate;
	}
	
	@JsonProperty("Rate")
	public void setRate(double rate) {
		Rate = rate;
	}
	
	@JsonProperty("Quantity")
	public Double getQuantity() {
		return Quantity;
	}
	
	@JsonProperty("Quantity")
	public void setQuantity(double quantity) {
		Quantity = quantity;
	}
	
	
}
