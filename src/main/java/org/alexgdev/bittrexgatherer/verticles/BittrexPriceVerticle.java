package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;

import org.alexgdev.bittrexgatherer.dto.MovingAverageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.service.OrderFillService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String GET_MA = "GETMA";
	
	private ArrayList<OrderFillDTO> fillsList = new ArrayList<OrderFillDTO>();
	private String tradingPair;
	private Long timerID;
	private int timeInterval;
	private MovingAverageDTO dto;
	
	private OrderFillService service;
	
	public BittrexPriceVerticle(OrderFillService service){
		super();
		this.service = service;
	}
	
	@Override
    public void start() throws Exception {
        super.start();
		tradingPair = config().getString("tradingPair");
		timeInterval = 5*60*1000;
		dto = new MovingAverageDTO();
		dto.setTimeInterval(timeInterval);
		dto.setTradingPair(tradingPair);

        vertx.eventBus()
                .<String>consumer(HANDLE_FILLS+":"+tradingPair)
                .handler(processFills(service));
        vertx.eventBus()
        	.<String>consumer(GET_MA+":"+tradingPair)
        	.handler(getMovingAverage());
        timerID = vertx.setTimer(timeInterval, id -> {
	    	  calculateMovingAverage();
	    });
    }
	
	private void calculateMovingAverage(){
		vertx.<String>executeBlocking(future -> {
	           
            if(fillsList.size() == 0){
            	future.complete();
            } else {
            	double movingAverage = 0;
            	double volume = 0;
            	double volumeWeightedMovingAverage = 0;
            	for(OrderFillDTO o: fillsList){
            		movingAverage+= o.getRate();
            		volume += o.getQuantity();
            		volumeWeightedMovingAverage += o.getRate() * o.getQuantity();
            	}
            	
            	movingAverage = movingAverage/fillsList.size();
            	volumeWeightedMovingAverage = volumeWeightedMovingAverage / volume;
            	dto.setTimeStamp(System.currentTimeMillis());
            	dto.setMovingAverage(movingAverage);
            	dto.setVolumeWeightedMovingAverage(volumeWeightedMovingAverage);
            	
            	SharedData sd = vertx.sharedData();

            	LocalMap<String, Double> map1 = sd.getLocalMap(tradingPair+" Price");
            	map1.put("ma", movingAverage);
            	map1.put("vwma", volumeWeightedMovingAverage);
            	future.complete();
            	
            	
            }
			
        }, result -> {
            if (result.succeeded()) {
                System.out.println("Done processing price: "+dto);
                timerID = vertx.setTimer(timeInterval, id -> {
        	    	  calculateMovingAverage();
              	});
            } else {
            	System.out.println("Failed processing price");
            	timerID = vertx.setTimer(timeInterval, id -> {
        	    	  calculateMovingAverage();
              	});
            }
        });

	}
	
	private Handler<Message<String>> getMovingAverage(){
		return msg -> {
			String payload = JsonObject.mapFrom(dto).encodePrettily();
			msg.reply(payload);
		};
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

	

}
