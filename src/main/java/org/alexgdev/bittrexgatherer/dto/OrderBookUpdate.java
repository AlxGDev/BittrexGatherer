package org.alexgdev.bittrexgatherer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderBookUpdate {
	private String MarketName;
	private Long Nounce;
	private List<OrderDeltaDTO> Buys;
	private List<OrderDeltaDTO> Sells;
	private List<OrderFillDTO> Fills;
	
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
	public List<OrderDeltaDTO> getBuys() {
		return Buys;
	}
	
	@JsonProperty("Buys")
	public void setBuys(List<OrderDeltaDTO> buys) {
		Buys = buys;
	}
	
	@JsonProperty("Sells")
	public List<OrderDeltaDTO> getSells() {
		return Sells;
	}
	
	@JsonProperty("Sells")
	public void setSells(List<OrderDeltaDTO> sells) {
		Sells = sells;
	}
	
	@JsonProperty("Fills")
	public List<OrderFillDTO> getFills() {
		return Fills;
	}
	
	@JsonProperty("Fills")
	public void setFills(List<OrderFillDTO> fills) {
		Fills = fills;
	}
	

}
