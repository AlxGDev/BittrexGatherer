package org.alexgdev.bittrexgatherer.verticles;

import java.util.TreeMap;

import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderDeltaDTO;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class BittrexOrderBookVerticle extends AbstractVerticle{
	
	public static final String INIT_ORDERBOOK = "INITORDERBOOK";
	public static final String UPDATE_ORDERBOOK = "UPDATEORDERBOOK";
	
	private TreeMap<Double, Double> buyOrders = new TreeMap<Double, Double>();
	private TreeMap<Double, Double> sellOrders = new TreeMap<Double, Double>();
	private String tradingPair;
	
	
	@Override
    public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
        super.start();
        vertx.eventBus()
                .<String>consumer(INIT_ORDERBOOK+":"+tradingPair)
                .handler(processInit());
        vertx.eventBus()
        		.<String>consumer(UPDATE_ORDERBOOK+":"+tradingPair)
        		.handler(processOrderBookUpdate());
    }
	
	private Handler<Message<String>> processInit() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            JsonObject payload = new JsonObject(msg.body());
	            JsonArray buys = payload.getJsonArray("Buys");
	            
				for(int i = 0; i<buys.size();i++){
					buyOrders.put(buys.getJsonObject(0).getDouble("Rate"), buys.getJsonObject(0).getDouble("Quantity"));
				}
				JsonArray sells = payload.getJsonArray("Sells");
				for(int i = 0; i<buys.size();i++){
					sellOrders.put(sells.getJsonObject(0).getDouble("Rate"), sells.getJsonObject(0).getDouble("Quantity"));
				}
				future.complete();
        	} catch(Exception e){
        		e.printStackTrace();
        		future.fail(e);
        	}
        }, result -> {
            if (result.succeeded()) {
                System.out.println("Done setting up Order Book: BuyPrice "+buyOrders.lastKey()+" Quantity: "+buyOrders.get(buyOrders.lastKey()));
            } else {
            	System.out.println("Failed setting up Order Book");
            }
        });
    }
	
	private Handler<Message<String>> processOrderBookUpdate() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	OrderBookUpdate update = new JsonObject(msg.body()).mapTo(OrderBookUpdate.class);
            for(OrderDeltaDTO dto: update.getBuys()){
            	if(dto.getQuantity() == 0.0){
            		buyOrders.remove(dto.getRate());
            	} else {
            		buyOrders.put(dto.getRate(), dto.getQuantity());
            	}
            }
            for(OrderDeltaDTO dto: update.getSells()){
            	if(dto.getQuantity() == 0.0){
            		sellOrders.remove(dto.getRate());
            	} else {
            		sellOrders.put(dto.getRate(), dto.getQuantity());
            	}
            }
            future.complete();		
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
