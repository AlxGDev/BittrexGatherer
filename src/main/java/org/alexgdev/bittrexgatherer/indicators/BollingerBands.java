package org.alexgdev.bittrexgatherer.indicators;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

import lombok.Data;

@Data
public class BollingerBands {
	
	private MovingStatistic ma;
	private Integer k;
	private Double upperBand;
	private Double lowerBand;
	
	public BollingerBands(MovingStatistic ma, Integer k){
		this.ma = ma;
		this.k = k;
	}
	
	public void add(PriceEvent e) {        
        ma.add(e);
        upperBand = ma.getAverage()+k*ma.getStdDev();
        lowerBand = ma.getAverage()-k*ma.getStdDev();
        
    }
}
