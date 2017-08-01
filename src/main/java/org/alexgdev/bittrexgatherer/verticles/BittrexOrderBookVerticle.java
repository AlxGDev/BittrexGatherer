package org.alexgdev.bittrexgatherer.verticles;

import java.util.TreeMap;

import org.alexgdev.bittrexgatherer.dto.OrderBookDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderDeltaDTO;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class BittrexOrderBookVerticle extends AbstractVerticle{
	
	public static final String INIT_ORDERBOOK = "INITORDERBOOK";
	public static final String ORDERBOOK_READY = "ORDERBOOKREADY";
	public static final String UPDATE_ORDERBOOK = "UPDATEORDERBOOK";
	public static final String GET_ORDERBOOK = "GETORDERBOOK";
	
	private OrderBookDTO orderBook;
	private String tradingPair;
	
	
	@Override
    public void start() throws Exception {
        super.start();
        tradingPair = config().getString("tradingPair");
        orderBook = new OrderBookDTO();
        orderBook.setTradingPair(tradingPair);
        vertx.eventBus()
                .<String>consumer(INIT_ORDERBOOK+":"+tradingPair)
                .handler(processInit());
        /*vertx.eventBus()
        		.<String>consumer(UPDATE_ORDERBOOK+":"+tradingPair)
        		.handler(processOrderBookUpdate()); */
        vertx.eventBus()
        	.<String>consumer(GET_ORDERBOOK+":"+tradingPair)
        	.handler(getOrderBook());
    }
	
	private Handler<Message<String>> processInit() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            JsonObject payload = new JsonObject(msg.body());
	            JsonArray buys = payload.getJsonArray("Buys");
	            
				for(int i = 0; i<buys.size();i++){
					orderBook.getBuyOrders().put(buys.getJsonObject(i).getDouble("Rate"), buys.getJsonObject(i).getDouble("Quantity"));
				}
				JsonArray sells = payload.getJsonArray("Sells");
				for(int i = 0; i<buys.size();i++){
					orderBook.getSellOrders().put(sells.getJsonObject(i).getDouble("Rate"), sells.getJsonObject(i).getDouble("Quantity"));
				}
				future.complete();
        	} catch(Exception e){
        		e.printStackTrace();
        		future.fail(e);
        	}
        }, result -> {
            if (result.succeeded()) {
            	vertx.eventBus().<String>send(ORDERBOOK_READY+":"+tradingPair, "ready");
                System.out.println("Done setting up Order Book: BuyPrice "+orderBook.getBuyOrders().lastKey()+" Quantity: "+orderBook.getBuyOrders().get(orderBook.getBuyOrders().lastKey()));
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
            		orderBook.getBuyOrders().remove(dto.getRate());
            	} else {
            		orderBook.getBuyOrders().put(dto.getRate(), dto.getQuantity());
            	}
            }
            for(OrderDeltaDTO dto: update.getSells()){
            	if(dto.getQuantity() == 0.0){
            		orderBook.getSellOrders().remove(dto.getRate());
            	} else {
            		orderBook.getSellOrders().put(dto.getRate(), dto.getQuantity());
            	}
            }
            future.complete();		
        }, result -> {
            if (result.succeeded()) {
                System.out.println("Done processing OB update");
                msg.reply(result.result());
            } else {
            	System.out.println("Failed processing OB update");
            	msg.reply(result.result());
            }
        });
    }
	
	private Handler<Message<String>> getOrderBook(){
		return msg -> {
			String payload = JsonObject.mapFrom(orderBook).toString();
			msg.reply(payload);
		};
	}

}
