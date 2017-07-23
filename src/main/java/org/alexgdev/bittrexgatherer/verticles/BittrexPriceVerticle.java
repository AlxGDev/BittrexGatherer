package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;

import org.alexgdev.bittrexgatherer.dto.MessageDTO;
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

@Component
public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String CALCULATE_MA = "CALCULATEMA";
	
	private HashMap<String, ArrayList<OrderFillDTO>> fillsMap = new HashMap<String, ArrayList<OrderFillDTO>>();
	
	@Autowired
	private OrderFillService service;
	
	@Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus()
                .<String>consumer(HANDLE_FILLS)
                .handler(processFills(service));
        vertx.eventBus()
        		.<String>consumer(CALCULATE_MA)
        		.handler(processPriceCalc());
    }
	
	private Handler<Message<String>> processFills(OrderFillService service) {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            MessageDTO dto = new JsonObject(msg.body()).mapTo(MessageDTO.class);
	            ArrayList<OrderFillDTO> currentFills = fillsMap.get(dto.getMessage());
	            if(currentFills == null){
	            	currentFills = new ArrayList<OrderFillDTO>();
	            }
				for(OrderFillDTO fill : dto.getPayload().getFills()){
					currentFills.add(fill);
					service.save(fill, dto.getMessage(), "bittrex");
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
            ArrayList<OrderFillDTO> currentFills = fillsMap.get(dto.getMessage());
            if(currentFills == null || currentFills.size() == 0){
            	future.complete();
            } else {
            	double movingAverage = 0;
            	double volume = 0;
            	double volumeWeightedAverage = 0;
            	for(OrderFillDTO o: currentFills){
            		movingAverage+= o.getRate();
            		volume += o.getQuantity();
            		volumeWeightedAverage += o.getRate() * o.getQuantity();
            	}
            	fillsMap.put(dto.getMessage(), new ArrayList<OrderFillDTO>());
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
