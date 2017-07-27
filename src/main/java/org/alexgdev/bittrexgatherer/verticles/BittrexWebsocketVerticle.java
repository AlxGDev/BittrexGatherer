package org.alexgdev.bittrexgatherer.verticles;


import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.springframework.stereotype.Component;



import io.vertx.core.AbstractVerticle;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class BittrexWebsocketVerticle extends AbstractVerticle {

	  private String tradingPair;
	  private String handleFillsMessage;
	  private String initOrderBookMessage;
	  private String updateOrderBookMessage;
	  private String calculatePriceMessage;
	  private Long timerID;
	  
	  @Override
	  public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
		handleFillsMessage = BittrexPriceVerticle.HANDLE_FILLS+":"+tradingPair;
		calculatePriceMessage = BittrexPriceVerticle.CALCULATE_MA+":"+tradingPair;
		initOrderBookMessage = BittrexOrderBookVerticle.INIT_ORDERBOOK+":"+tradingPair;
		updateOrderBookMessage = BittrexOrderBookVerticle.UPDATE_ORDERBOOK+":"+tradingPair;
		
		String endpoint = "/signalr/connect?transport=webSockets&clientProtocol="+config().getString("protocol")+
						  "&connectionToken="+config().getString("connectionToken")+
						  //"&connectionData="+config().getString("connectionData")+
						  "&tid="+config().getString("tid");
		
		JsonObject msg1 = new JsonObject().put("H", "corehub")
						.put("M", "SubscribeToExchangeDeltas")
						.put("A", new JsonArray().add(config().getString("tradingPair")))
						.put("I", 0);
		
		JsonObject msg2 = new JsonObject().put("H", "corehub")
						.put("M", "QueryExchangeState")
						.put("A", new JsonArray().add(config().getString("tradingPair")))
						.put("I", 1);
		
		
		HttpClientOptions options = new HttpClientOptions();
		options.setMaxWebsocketFrameSize(300000);
		options.setMaxWebsocketMessageSize(300000);
	    HttpClient client = vertx.createHttpClient(options);
	    
	    client.websocket(80, "socket.bittrex.com", endpoint, 
	    websocket -> {
	      websocket.handler(data -> {
	    	  System.out.println("Received data " + data.toString("ISO-8859-1"));
	    	  JsonObject msg = data.toJsonObject();
	    	  if(msg.containsKey("R") && msg.getString("I").equals("1")){
	    		  vertx.eventBus().<String>send(initOrderBookMessage, msg.getJsonObject("R").encodePrettily());
	    	  }
	    	  if(msg.containsKey("M") 
	    		&& msg.getJsonArray("M").size() > 0 
	    		&& msg.getJsonArray("M").getJsonObject(0).getString("M").equals("updateExchangeState")
	    		&& msg.getJsonArray("M").getJsonObject(0).containsKey("A")
	    		&& msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").size() > 0
	    		&& msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).getJsonArray("Fills").size()>0){
	    		  
	    		  OrderBookUpdate payload = msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).mapTo(OrderBookUpdate.class);
	    		  
	    		  vertx.eventBus().<String>send(handleFillsMessage, JsonObject.mapFrom(payload).encodePrettily());

	    	  } 
	    	  
	      });
	      
	      
	      websocket.writeTextMessage(msg2.encodePrettily());
	      websocket.writeTextMessage(msg1.encodePrettily());
	      
	    });
	    
	    timerID = vertx.setTimer(5*60*1000, id -> {
	    	  calculateMovingAverage();
	    });
	   
	}
	
	private void calculateMovingAverage(){
		vertx.eventBus()
        .<String>send(calculatePriceMessage, "", result -> {
            if (result.succeeded()) {
            	timerID = vertx.setTimer(5*60*1000, id -> {
      	    	  calculateMovingAverage();
            	});
            } else {
            	timerID = vertx.setTimer(5*60*1000, id -> {
      	    	  calculateMovingAverage();
            	});
            }
        });
	}
	
	
}
