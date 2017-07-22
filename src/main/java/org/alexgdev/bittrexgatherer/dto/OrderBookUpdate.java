package org.alexgdev.bittrexgatherer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderBookUpdate {
	private String MarketName;
	private Long Nounce;
	private List<OrderDelta> Buys;
	private List<OrderDelta> Sells;
	private List<OrderFill> Fills;
	
	@JsonProperty("MarketName")
	public String getMarketName() {
		return MarketName;
	}
	
	@JsonProperty("MarketName")
	public void setMarketName(String marketName) {
		MarketName = marketName;
	}
	
	@JsonProperty("Nounce")
	public Long getNounce() {
		return Nounce;
	}
	
	@JsonProperty("Nounce")
	public void setNounce(Long nounce) {
		Nounce = nounce;
	}
	
	@JsonProperty("Buys")
	public List<OrderDelta> getBuys() {
		return Buys;
	}
	
	@JsonProperty("Buys")
	public void setBuys(List<OrderDelta> buys) {
		Buys = buys;
	}
	
	@JsonProperty("Sells")
	public List<OrderDelta> getSells() {
		return Sells;
	}
	
	@JsonProperty("Sells")
	public void setSells(List<OrderDelta> sells) {
		Sells = sells;
	}
	
	@JsonProperty("Fills")
	public List<OrderFill> getFills() {
		return Fills;
	}
	
	@JsonProperty("Fills")
	public void setFills(List<OrderFill> fills) {
		Fills = fills;
	}
	

}
