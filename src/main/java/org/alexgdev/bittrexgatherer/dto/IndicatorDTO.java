package org.alexgdev.bittrexgatherer.dto;

import java.text.DecimalFormat;
import java.util.Map.Entry;

import org.alexgdev.bittrexgatherer.indicators.BollingerBands;
import org.alexgdev.bittrexgatherer.indicators.RSI;
import org.alexgdev.bittrexgatherer.indicators.SimpleMovingAverage;
import org.alexgdev.bittrexgatherer.indicators.VolumeWeightedMovingAverage;
import org.alexgdev.bittrexgatherer.util.MarketTick;

import io.vertx.core.json.JsonObject;
import lombok.Data;

@Data
public class IndicatorDTO {
	
	private Double currentPrice;
	private RSI rsi;
	private BollingerBands bb;
	private SimpleMovingAverage sma;
	private VolumeWeightedMovingAverage vwma;
	
	public JsonObject convertToJson(){
		JsonObject json = new JsonObject();
		DecimalFormat df=new DecimalFormat("0.00000000");
		json.put("tick", df.format(currentPrice));
		json.put("rsi", df.format(rsi.getRSI()));
		json.put("bb_high", df.format(bb.getUpperBand()));
		json.put("bb_low", df.format(bb.getLowerBand()));
		json.put("sma", df.format(sma.getAverage()));
		json.put("vwma", df.format(vwma.getAverage()));
		return json;
	}
}
