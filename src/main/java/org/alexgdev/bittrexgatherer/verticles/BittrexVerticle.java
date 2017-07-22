package org.alexgdev.bittrexgatherer.verticles;

import org.alexgdev.bittrexgatherer.dto.MessageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.springframework.stereotype.Component;



import io.vertx.core.AbstractVerticle;

import io.vertx.core.http.HttpClient;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


@Component
public class BittrexVerticle extends AbstractVerticle {

	  private String tradingPair;
	  private Long timerID;
	  
	  @Override
	  public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
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
		
		
		
	    HttpClient client = vertx.createHttpClient();
	    client.websocket(80, "socket.bittrex.com", endpoint, 
	    websocket -> {
	      websocket.handler(data -> {
	    	  JsonObject msg = data.toJsonObject();
	    	  if(msg.containsKey("M") 
	    		&& msg.getJsonArray("M").size() > 0 
	    		&& msg.getJsonArray("M").getJsonObject(0).getString("M").equals("updateExchangeState")
	    		&& msg.getJsonArray("M").getJsonObject(0).containsKey("A")
	    		&& msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").size() > 0
	    		&& msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).getJsonArray("Fills").size()>0){
	    		  MessageDTO dto = new MessageDTO();
	    		  dto.setMessage(tradingPair);
	    		  OrderBookUpdate payload = msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).mapTo(OrderBookUpdate.class);
	    		  dto.setPayload(payload);
	    		  
	    		  vertx.eventBus()
	    	        .<String>send(BittrexPriceVerticle.HANDLE_FILLS, JsonObject.mapFrom(dto).encodePrettily());
	    		  
	    		  System.out.println("Received data " + data.toString("ISO-8859-1"));
	    	  } 
	    	  
	      });
	      
	      websocket.writeTextMessage(msg1.encodePrettily());
	      //websocket.writeTextMessage(msg2.encodePrettily());
	      
	    });
	    
	    timerID = vertx.setTimer(5*60*1000, id -> {
	    	  calculateMovingAverage();
	    });
	   
	}
	
	private void calculateMovingAverage(){
		MessageDTO dto = new MessageDTO();
		dto.setMessage(tradingPair);
		vertx.eventBus()
        .<String>send(BittrexPriceVerticle.CALCULATE_MA, JsonObject.mapFrom(dto).encodePrettily(), result -> {
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
