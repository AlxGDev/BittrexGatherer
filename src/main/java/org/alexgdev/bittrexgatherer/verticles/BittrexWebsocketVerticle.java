package org.alexgdev.bittrexgatherer.verticles;


import org.springframework.web.util.UriComponentsBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;


public class BittrexWebsocketVerticle extends AbstractVerticle {

	  private String tradingPair;
	  private String handleFillsMessage;
	  private String initOrderBookMessage;
	  private String updateOrderBookMessage;
	  private Long timerID1;
	  private Long timerID2;
	  
	  private String endPoint;
	  private HttpClient client;
	  private WebClient restclient;
	 
	  
	  @Override
	  public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
		handleFillsMessage = BittrexPriceVerticle.HANDLE_FILLS+":"+tradingPair;
		initOrderBookMessage = BittrexOrderBookVerticle.INIT_ORDERBOOK+":"+tradingPair;
		updateOrderBookMessage = BittrexOrderBookVerticle.UPDATE_ORDERBOOK+":"+tradingPair;
		
		WebClientOptions options = new WebClientOptions().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0.1 Waterfox/54.0.1");
	    options.setKeepAlive(false);
		restclient = WebClient.create(vertx, options);
		getConnectionToken();
		
		
		
		
		
		
	  }
	  
	  private void setupHttpClient(){
		 
		  if(client != null){
			  client.close();
		  }
		  HttpClientOptions options = new HttpClientOptions();
		  options.setMaxWebsocketFrameSize(300000);
		  options.setMaxWebsocketMessageSize(300000);
		  client = vertx.createHttpClient(options);
		  connectToBittrex();
	  }
	  
	  private void getConnectionToken() {
		  restclient
		  .get(80, "socket.bittrex.com", "/signalr/negotiate")
		  .addQueryParam("clientProtocol", "1.5")
		  .addQueryParam("connectionData", "[{\"name\":\"corehub\"}]")
		  .as(BodyCodec.jsonObject())
		  .send(ar -> {
		    if (ar.succeeded()) {
		    	HttpResponse<JsonObject> response = ar.result();
		    	if(response.statusCode() != 200){
		    		 System.err.println("Error while trying to get connection token, retrying in 5: " + response.statusCode());
		    		 timerID2 = vertx.setTimer(5*1000, id -> {
		    			 getConnectionToken();
	              	});
		    	} else {
		    		JsonObject body = response.body();
		    		
		    		System.out.println("Successfully retrieved connection Token");
		    		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString("/signalr/connect");
		    		urlBuilder.queryParam("transport", "webSockets");
		    		urlBuilder.queryParam("clientProtocol", body.getString("ProtocolVersion"));
		    		urlBuilder.queryParam("connectionToken", body.getString("ConnectionToken"));
		    		urlBuilder.queryParam("connectionData", "[{\"name\":\"corehub\"}]");
		    		
		    		endPoint = urlBuilder.build().encode().toUriString();
		    		
		    		setupHttpClient();		  
		    		
		    	}
		        

		      
		    } else {
		      System.err.println("Error while trying to get connection token, retrying in 5: " + ar.cause().getMessage());
		      timerID2 = vertx.setTimer(5*1000, id -> {
	    			 getConnectionToken();
           	});
		    }
		  });
	  }
	  
	  private void connectToBittrex(){
		 
		  client.websocket(80, "socket.bittrex.com", endPoint, 
		    		bittrexWebSocketHandler(), failure -> {
		                System.err.println("Failure connecting to Bittrex Websocket. Retrying in 5: "+failure.getMessage() );
		                timerID1 = vertx.setTimer(5*1000, id -> {
		        	    	  connectToBittrex();
		              	});
		            });
	  }
	
	  private Handler<WebSocket> bittrexWebSocketHandler(){
		 
		  JsonObject msg1 = new JsonObject().put("H", "corehub")
				.put("M", "SubscribeToExchangeDeltas")
				.put("A", new JsonArray().add(config().getString("tradingPair")))
				.put("I", 0);

		  JsonObject msg2 = new JsonObject().put("H", "corehub")
				.put("M", "QueryExchangeState")
				.put("A", new JsonArray().add(config().getString("tradingPair")))
				.put("I", 1);


		  return websocket -> {
		      websocket.handler(data -> {
		    	  //System.out.println("Received data " + data.toString("ISO-8859-1"));
		    	  JsonObject msg = data.toJsonObject();
		    	  if(msg.containsKey("R") && msg.getString("I").equals("1")){
		    		  
		    		  vertx.eventBus().<JsonObject>publish(initOrderBookMessage, msg.getJsonObject("R"));
		    	  }
		    	  if(msg.containsKey("M") 
		    		&& msg.getJsonArray("M").size() > 0 
		    		&& msg.getJsonArray("M").getJsonObject(0).getString("M").equals("updateExchangeState")
		    		&& msg.getJsonArray("M").getJsonObject(0).containsKey("A")
		    		&& msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").size() > 0){
		    		  JsonObject payload = msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0);
		    		  //OrderBookUpdate payload = msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).mapTo(OrderBookUpdate.class);
		    		  if(msg.getJsonArray("M").getJsonObject(0).getJsonArray("A").getJsonObject(0).getJsonArray("Fills").size()>0){
		    			  vertx.eventBus().<JsonObject>publish(handleFillsMessage, payload);
		    		  }
		    		  
		    		  	  vertx.eventBus().<JsonObject>publish(updateOrderBookMessage, payload);
		    		  
		    		 
		    	  } 
		    	  
		      });
		      
		      
		      websocket.writeTextMessage(msg2.encode());
		      websocket.writeTextMessage(msg1.encode());
		      
		  };
	  }
	
	
	
	
}
