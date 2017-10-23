package org.alexgdev.bittrexgatherer.verticles;

import java.util.TreeMap;

import org.alexgdev.bittrexgatherer.dto.OrderBookDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderDeltaDTO;
import org.alexgdev.bittrexgatherer.util.MessageDefinitions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class BittrexOrderBookVerticle extends AbstractVerticle{

	
	private OrderBookDTO orderBook;
	private String tradingPair;
	
	
	@Override
    public void start() throws Exception {
        super.start();
        tradingPair = config().getString("tradingPair");
        orderBook = new OrderBookDTO();
        orderBook.setTradingPair(tradingPair);
        vertx.eventBus()
                .<JsonObject>consumer(MessageDefinitions.INIT_ORDERBOOK+":"+tradingPair)
                .handler(processInit());
        vertx.eventBus()
        		.<JsonObject>consumer(MessageDefinitions.UPDATE_ORDERBOOK+":"+tradingPair)
        		.handler(processOrderBookUpdate()); 
        vertx.eventBus()
        	.<JsonObject>consumer(MessageDefinitions.GET_ORDERBOOK+":"+tradingPair)
        	.handler(getOrderBook());
    }
	
	private Handler<Message<JsonObject>> processInit() {
        return msg -> {
        	try{
	            JsonObject payload = msg.body();
	            JsonArray buys = payload.getJsonArray("Buys");
	            
				for(int i = 0; i<buys.size();i++){
					orderBook.getBuyOrders().put(buys.getJsonObject(i).getDouble("Rate"), buys.getJsonObject(i).getDouble("Quantity"));
				}
				JsonArray sells = payload.getJsonArray("Sells");
				for(int i = 0; i<sells.size();i++){
					orderBook.getSellOrders().put(sells.getJsonObject(i).getDouble("Rate"), sells.getJsonObject(i).getDouble("Quantity"));
				}
				vertx.eventBus().publish(MessageDefinitions.ORDERBOOK_READY+":"+tradingPair, "ready");
        	} catch(Exception e){
        		e.printStackTrace();
        		System.out.println("Failed setting up Order Book");
        	}
        };
    }
	
	
	
	private Handler<Message<JsonObject>> processOrderBookUpdate() {
        return msg -> {
        	 //System.out.println(msg.body());
        	OrderBookUpdate update = msg.body().mapTo(OrderBookUpdate.class);
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
            	
        };
    }
	
	private Handler<Message<JsonObject>> getOrderBook(){
		return msg -> {
			JsonObject payload = orderBook.convertToJson();
			msg.reply(payload);
		};
	}

}
