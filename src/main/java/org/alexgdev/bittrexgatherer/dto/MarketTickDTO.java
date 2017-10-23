package org.alexgdev.bittrexgatherer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MarketTickDTO {
	@JsonProperty("O")
	private Double open;
	@JsonProperty("L")
	private Double low;
	@JsonProperty("H")
	private Double high;
	@JsonProperty("C")
	private Double close;
	@JsonProperty("BV")
	private Double baseVolume;
	@JsonProperty("V")
	private Double volume;
	@JsonProperty("T")
	private String timeStamp;
	
}
