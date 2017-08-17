package org.alexgdev.bittrexgatherer.util;

import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;

import lombok.Data;

@Data
public class MarketTick {
	
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
	public Double getAverage(){
		return (high+low)/2;
	}
	
	public void clearPeriod(){
		high = 0.0;
		low = 0.0;
		volume = 0.0;
	}

}
