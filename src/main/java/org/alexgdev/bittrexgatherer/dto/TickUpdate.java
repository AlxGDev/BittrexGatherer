package org.alexgdev.bittrexgatherer.dto;

import java.util.List;

import lombok.Data;

@Data
public class TickUpdate {
	private Integer interval;
	private List<MarketTickDTO> ticks;
}
