package org.alexgdev.bittrexgatherer.dto;

import lombok.Data;

@Data
public class MovingAverageDTO {
	private String tradingPair;
	private long timeStamp;
	private int timeInterval;
	private double movingAverage;
	private double volumeWeightedMovingAverage;
}
