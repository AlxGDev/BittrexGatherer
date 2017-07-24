package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;

import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String CALCULATE_MA = "CALCULATEMA";
	
	private ArrayList<OrderFillDTO> fillsList = new ArrayList<OrderFillDTO>();
	private String tradingPair;
	
	private OrderFillService service;
	
	public BittrexPriceVerticle(OrderFillService service){
		super();
		this.service = service;
	}
	
	@Override
    public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
        super.start();
        vertx.eventBus()
                .<String>consumer(HANDLE_FILLS+":"+tradingPair)
                .handler(processFills(service));
        vertx.eventBus()
        		.<String>consumer(CALCULATE_MA+":"+tradingPair)
        		.handler(processPriceCalc());
    }
	
	private Handler<Message<String>> processFills(OrderFillService service) {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            OrderBookUpdate update = new JsonObject(msg.body()).mapTo(OrderBookUpdate.class);
	            
				for(OrderFillDTO fill : update.getFills()){
					fillsList.add(fill);
					service.save(fill, tradingPair, "bittrex");
				}
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
           
            if(fillsList.size() == 0){
            	future.complete();
            } else {
            	double movingAverage = 0;
            	double volume = 0;
            	double volumeWeightedAverage = 0;
            	for(OrderFillDTO o: fillsList){
            		movingAverage+= o.getRate();
            		volume += o.getQuantity();
            		volumeWeightedAverage += o.getRate() * o.getQuantity();
            	}
            	
            	movingAverage = movingAverage/fillsList.size();
            	volumeWeightedAverage = volumeWeightedAverage / volume;
            	System.out.println(tradingPair+" MA: "+movingAverage+" VWMA: "+volumeWeightedAverage);
            	
            	SharedData sd = vertx.sharedData();

            	LocalMap<String, Double> map1 = sd.getLocalMap(tradingPair+" Price");
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
