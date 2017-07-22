package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;

import org.alexgdev.bittrexgatherer.dto.MessageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderFill;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String CALCULATE_MA = "CALCULATEMA";
	
	private HashMap<String, ArrayList<OrderFill>> fillsMap = new HashMap<String, ArrayList<OrderFill>>();
	
	@Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus()
                .<String>consumer(HANDLE_FILLS)
                .handler(processFills());
        vertx.eventBus()
        		.<String>consumer(CALCULATE_MA)
        		.handler(processPriceCalc());
    }
	
	private Handler<Message<String>> processFills() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            MessageDTO dto = new JsonObject(msg.body()).mapTo(MessageDTO.class);
	            ArrayList<OrderFill> currentFills = fillsMap.get(dto.getMessage());
	            if(currentFills == null){
	            	currentFills = new ArrayList<OrderFill>();
	            }
				for(OrderFill fill : dto.getPayload().getFills()){
					currentFills.add(fill);
				}
				fillsMap.put(dto.getMessage(), currentFills);
				future.complete();
        	} catch(Exception e){
        		e.printStackTrace();
        		future.fail(e);
        	}
        }, result -> {
            if (result.succeeded()) {
                System.out.println("Done processing fills");
            } else {
            	System.out.println("Failed processing fills");
            }
        });
    }
	
	private Handler<Message<String>> processPriceCalc() {
        return msg -> vertx.<String>executeBlocking(future -> {
            MessageDTO dto = new JsonObject(msg.body()).mapTo(MessageDTO.class);
            ArrayList<OrderFill> currentFills = fillsMap.get(dto.getMessage());
            if(currentFills == null || currentFills.size() == 0){
            	future.complete();
            } else {
            	double movingAverage = 0;
            	double volume = 0;
            	double volumeWeightedAverage = 0;
            	for(OrderFill o: currentFills){
            		movingAverage+= o.getRate();
            		volume += o.getQuantity();
            		volumeWeightedAverage += o.getRate() * o.getQuantity();
            	}
            	fillsMap.put(dto.getMessage(), new ArrayList<OrderFill>());
            	movingAverage = movingAverage/currentFills.size();
            	volumeWeightedAverage = volumeWeightedAverage / volume;
            	System.out.println(dto.getMessage()+" MA: "+movingAverage+" VWMA: "+volumeWeightedAverage);
            	
            	SharedData sd = vertx.sharedData();

            	LocalMap<String, Double> map1 = sd.getLocalMap(dto.getMessage()+" Price");
            	map1.put("ma", movingAverage);
            	map1.put("vwma", volumeWeightedAverage);
            	future.complete();
            	
            	
            }
			
        }, result -> {
            if (result.succeeded()) {
                System.out.println("Done processing price");
                msg.reply(result.result());
            } else {
            	System.out.println("Failed processing price");
            	msg.reply(result.result());
            }
        });
    }

}
