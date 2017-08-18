package org.alexgdev.bittrexgatherer.dto;

import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import io.vertx.core.json.JsonObject;
import lombok.Data;
@Data
public class OrderBookDTO {
	private String tradingPair;
	private SortedMap<Double, Double> buyOrders = new TreeMap<Double, Double>();
	private SortedMap<Double, Double> sellOrders = new TreeMap<Double, Double>();
	
	public JsonObject convertToJson(){
		JsonObject json = new JsonObject();
		json.put("tradingPair", tradingPair);
		JsonObject buyOrders = new JsonObject();
		DecimalFormat df=new DecimalFormat("0.00000000");
		for(Entry<Double, Double> entry: this.buyOrders.entrySet()){
			
			buyOrders.put(df.format(entry.getKey().doubleValue()), entry.getValue().doubleValue());
		}
		json.put("buyOrders", buyOrders);
		JsonObject sellOrders = new JsonObject();
		for(Entry<Double, Double> entry: this.sellOrders.entrySet()){
			sellOrders.put(df.format(entry.getKey().doubleValue()), entry.getValue().doubleValue());
		}
		json.put("sellOrders", sellOrders);
		return json;
	}

}
