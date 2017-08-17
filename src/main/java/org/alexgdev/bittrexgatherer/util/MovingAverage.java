package org.alexgdev.bittrexgatherer.util;

import java.util.ArrayDeque;
import java.util.Queue;

import lombok.Data;

@Data
public class MovingAverage {
	private String id;
    private final Queue<Double> window = new ArrayDeque<>();
    private final int period;
    private Double sum = 0.0;

    public MovingAverage(int period, String id) {
    	this.id = id;
        this.period = period;
    }

    public void add(Double num) {
        sum = sum + num;
        window.add(num);
        if (window.size() > period) {
            sum = sum -(window.remove());
        }
    }

    public Double getAverage() {
        if (window.isEmpty()) return 0.0;
        return sum / window.size();
    }
}