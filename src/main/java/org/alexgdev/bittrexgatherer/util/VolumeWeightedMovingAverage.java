package org.alexgdev.bittrexgatherer.util;

import java.util.ArrayDeque;
import java.util.Queue;

import lombok.Data;

@Data
public class VolumeWeightedMovingAverage {
	
	private String id;
    private final Queue<Double> windowPrice = new ArrayDeque<>();
    private final Queue<Double> windowVolume = new ArrayDeque<>();
    private final int period;
    private Double sumTPV = 0.0;
    private double sumV = 0.0;

    public VolumeWeightedMovingAverage(int period, String id) {
    	this.id = id;
        this.period = period;
    }

    public void add(Double price, Double volume) {
        sumTPV += (price*volume);
        sumV += volume;
        windowPrice.add(price*volume);
        windowVolume.add(volume);
        if (windowPrice.size() > period) {
            sumTPV = sumTPV -(windowPrice.remove());
            sumV = sumV -(windowVolume.remove());
        }
    }

    public Double getAverage() {
        if (windowPrice.isEmpty()) return 0.0;
        return (sumTPV/sumV);
    }

}
