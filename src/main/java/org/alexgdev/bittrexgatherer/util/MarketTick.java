package org.alexgdev.bittrexgatherer.util;

import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;

import lombok.Data;

@Data
public class MarketTick implements PriceEvent{
	
	private Double high = 0.0;
	private Double low = 0.0;
	private Double volume = 0.0;
	
	
	public void add(OrderFillDTO dto){
		if(dto.getRate() > high){
			high = dto.getRate();
		} else if(dto.getRate() < low || low == 0.0){
			low = dto.getRate();
		}
		volume += dto.getQuantity();
	}
	
	public void clearPeriod(){
		high = 0.0;
		low = 0.0;
		volume = 0.0;
	}
	@Override
	public Double getRate() {
		return (high+low)/2;
	}
	@Override
	public Double getQuantity() {
		return volume;
	}

}
