package org.alexgdev.bittrexgatherer.dto;

import lombok.Data;

@Data
public class MovingAverageDTO {
	private String tradingPair;
	private Long timeStamp;
	private Double movingAverage;
	//private Double volumeWeightedMovingAverage;
}
