package org.alexgdev.bittrexgatherer.indicators;

import java.util.ArrayDeque;
import java.util.Queue;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

public class VolumeWeightedMovingAverage implements MovingStatistic{
	
	private String id;
    private final Queue<Double> windowPrice = new ArrayDeque<>();
    private final Queue<Double> windowVolume = new ArrayDeque<>();
    private final int period;
    private Double sumTPV = 0.0;
    private double sumV = 0.0;
    private Double avg = 0.0;
    private Double s;
    private Double variance = 0.0;
    private Double stdDev = 0.0;

    public VolumeWeightedMovingAverage(int period, String id) {
    	this.id = id;
        this.period = period;
    }

    public void add(PriceEvent e) {
    	Double price = e.getRate();
    	Double volume = e.getQuantity();
        sumTPV += (price*volume);
        sumV += volume;
        windowPrice.add(price);
        windowVolume.add(volume);

        if (windowPrice.size() == 1)
        {
            avg = price;
            s = 0.0;
            variance = 0.0;
            stdDev = 0.0;
        }
        else if(windowPrice.size()<=period)
        {
        	Double oldavg = avg;
        	Double oldS = s;
            avg = sumTPV/sumV;
            s = oldS + (price - oldavg)*(price - avg);
            variance = s/(windowPrice.size()-1);
            stdDev = Math.sqrt(variance);

        } else {
        	Double oldPriceValue =windowPrice.remove();
        	Double oldVolumeValue = windowVolume.remove();
        	sumTPV = sumTPV - (oldPriceValue*oldVolumeValue);
            sumV = sumV - oldVolumeValue;
        	avg = sumTPV/sumV;
        	variance = variance + (price-avg + oldPriceValue-avg)*(price - oldPriceValue)/(period-1);
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
