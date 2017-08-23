package org.alexgdev.bittrexgatherer.indicators;

import java.util.ArrayDeque;
import java.util.Queue;

import org.alexgdev.bittrexgatherer.util.PriceEvent;


public class SimpleMovingAverage implements MovingStatistic{
	private String id;
    private final Queue<Double> window = new ArrayDeque<>();
    private final int period;
    private Double avg = 0.0;
    private Double s;
    private Double variance = 0.0;
    private Double stdDev = 0.0;

    

    public SimpleMovingAverage(int period, String id) {
    	this.id = id;
        this.period = period;
    }

    public void add(PriceEvent e) {
    	Double price = e.getRate();
    	window.add(price);
        if (window.size() == 1) {
            avg = price;
            s = 0.0;
            variance = 0.0;
            stdDev = 0.0;
        } else if(window.size()<=period) {
        	Double oldavg = avg;
        	Double oldS = s;
            avg = avg + (price - oldavg)/window.size();
            s = oldS + (price - oldavg)*(e.getRate() - avg);
            variance = s/(window.size()-1);
            stdDev = Math.sqrt(variance);

        } else {
        	Double oldValue = window.remove();
        	avg = avg + (e.getRate()-oldValue)/period;
        	variance = variance + (price-avg + oldValue-avg)*(price - oldValue)/(period-1);
        	stdDev = Math.sqrt(variance);
        }
    	
    	
    	
    }

    public Double getAverage() {
    	return this.avg;
    }
    public Double getVariance() {
        return this.variance;
    }
    public Double getStdDev() {
    	return this.stdDev;
    }
    
    public String getId() {
    	return this.id;
    }
}