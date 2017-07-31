package org.alexgdev.bittrexgatherer.dto;

import java.util.SortedMap;
import java.util.TreeMap;

import lombok.Data;
@Data
public class OrderBookDTO {
	private String tradingPair;
	private SortedMap<Double, Double> buyOrders = new TreeMap<Double, Double>();
	private SortedMap<Double, Double> sellOrders = new TreeMap<Double, Double>();

}
