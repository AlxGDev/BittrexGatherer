package org.alexgdev.bittrexgatherer.verticles;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import org.springframework.web.util.UriComponentsBuilder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
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
	  
	  private HttpClient client;
	  private WebClient restclient;
	  private boolean isCloudFlareProtected = false;
	  private MultiMap headers = MultiMap.caseInsensitiveMultiMap();
	  
	  private int countTokenError = 0;
	  private String hcConnectionToken = "ZmtLmr61Y21k7X6PkBF+V+b0mccjfCkBA5bv7zUuK6oo0NRnx78xlA69L11pPKvaVOW8A9H4WGjPY3ocpAoTDC/owJH1IQEHSJwhx97xf+WObqYw";
	  private String hcProtocolVersion = "1.5";
	  private String cloudFlareUidCookie;
	  private String cloudFlareClearanceCookie;
	 
	  
	  @Override
	  public void start() throws Exception {
		tradingPair = config().getString("tradingPair");
		handleFillsMessage = BittrexPriceVerticle.HANDLE_FILLS+":"+tradingPair;
		initOrderBookMessage = BittrexOrderBookVerticle.INIT_ORDERBOOK+":"+tradingPair;
		updateOrderBookMessage = BittrexOrderBookVerticle.UPDATE_ORDERBOOK+":"+tradingPair;
		
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
		WebClientOptions options = new WebClientOptions().setUserAgent(userAgent);
		headers.add("User-Agent", userAgent);
	    
		options.setKeepAlive(false);
		restclient = WebClient.create(vertx, options);
		getConnectionToken();
		

		
	  }
	  
	  private void getConnectionToken() {
		  HttpRequest<Buffer> request =restclient
		  .get(80, "socket.bittrex.com", "/signalr/negotiate")		 
		  .addQueryParam("clientProtocol", "1.5")
		  .addQueryParam("connectionData", "[{\"name\":\"corehub\"}]");
		  
		  if(isCloudFlareProtected){
			  request.putHeader("Cookie", headers.get("Cookie"));
		  }

		  
		  request.send(ar -> {
			  
			  if (ar.succeeded()) {
				  HttpResponse<Buffer> response = ar.result();
				  if(response.statusCode() == 200){
					  countTokenError = 0;
					  JsonObject body = response.bodyAsJsonObject();
					  System.out.println("Successfully retrieved connection Token");
					  setupHttpClient(body.getString("ConnectionToken"), body.getString("ProtocolVersion"));
					  
				  } else if(response.statusCode()==503 && response.getHeader("Server").equals("cloudflare-nginx") && response.bodyAsString().contains("jschl_vc")){
					  System.err.println("Negotiation Endpoint is cloudflare protected, trying to bypass");	 
					  isCloudFlareProtected = true;
					  getCloudFlareToken();	 
		    				 
				  } else {
					  System.err.println("Error while trying to get connection token, retrying in 5: " + response.statusCode());
					  timerID2 = vertx.setTimer(5*1000, id -> {
			    			 getConnectionToken();
		              });
				  }
				  	  
		      
			  } else {
		    	countTokenError++;
	    		 if(countTokenError == 5){
	    			 countTokenError = 0;
	    			 System.err.println("Unable to get connection token, trying with hardcoded Token");
	    			 setupHttpClient(hcConnectionToken, hcProtocolVersion);	
	    		 } else {
	    			 System.err.println("Error while trying to get connection token, retrying in 5: " + ar.cause().getMessage());
		    		 timerID2 = vertx.setTimer(5*1000, id -> {
		    			 getConnectionToken();
	              	});
	    		 }
			  }
		  });
	  }
	  
	  private void getCloudFlareToken(){
		  vertx.<String>executeBlocking(future -> {

			  try(final com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient(BrowserVersion.CHROME)) {
				  	
				  webClient.getOptions().setJavaScriptEnabled(true);
				    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
				    webClient.getOptions().setRedirectEnabled(true);
				    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
				    webClient.getCache().setMaxSize(0);
				    webClient.waitForBackgroundJavaScript(5000);
				   // webClient.waitForBackgroundJavaScriptStartingBefore(20000);
			        String url = "https://socket.bittrex.com/signalr/negotiate?clientProtocol=1.5&connectionData=%5B%7B%22name%22%3A%22corehub%22%7D%5D&_=1504909744031";
			        HtmlPage htmlPage = webClient.getPage(url);
			        synchronized (htmlPage) {
			        	htmlPage.wait(7000);
			        }
			        
			        if(webClient.getCookieManager().getCookie("cf_clearance") != null && webClient.getCookieManager().getCookie("__cfduid") != null){
			        	String cookies ="__cfduid=" +
			        					webClient.getCookieManager().getCookie("__cfduid").getValue() +
			        					"; cf_clearance=" +
			        					webClient.getCookieManager().getCookie("cf_clearance").getValue();
			        	headers.add("Cookie", cookies);
			        	future.complete();
			        } else {
			        	future.fail("Failed to retrieve CloudFlare Cookies");
			        }
			        
			    } catch (FailingHttpStatusCodeException | IOException | InterruptedException e) {
					
					e.printStackTrace();
					future.fail(e);
				} 

		        

		      }, res -> {

		        if (res.succeeded()) {

		          System.out.println("Done htmlunit");
		          getConnectionToken();

		        } else {
		          System.out.println(res.cause().getMessage());
		        }
		});
		  
		  
	  }
	  
	  private void setupHttpClient(String connectionToken, String protocolVersion){
		 
		  if(client != null){
			  client.close();
		  }
		  
		  UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString("/signalr/connect");
  		  urlBuilder.queryParam("transport", "webSockets");
  		  urlBuilder.queryParam("clientProtocol", protocolVersion);
  		  urlBuilder.queryParam("connectionToken", connectionToken);
  		  urlBuilder.queryParam("connectionData", "[{\"name\":\"corehub\"}]");
  		
  		  String endPoint = urlBuilder.build().encode().toUriString();
		  
		  HttpClientOptions options = new HttpClientOptions();
		  
		  options.setMaxWebsocketFrameSize(1000000);
		  options.setMaxWebsocketMessageSize(1000000);
		  
		  client = vertx.createHttpClient(options);
		  connectToBittrex(endPoint);
	  }
	  
	  
	  
	  private void connectToBittrex(String endPoint){
		 
		  client.websocket(80, "socket.bittrex.com", endPoint, headers,
		    		bittrexWebSocketHandler(), failure -> {
		                System.err.println("Failure connecting to Bittrex Websocket. Retrying in 5: "+failure.getMessage() );
		                timerID1 = vertx.setTimer(5*1000, id -> {
		        	    	  connectToBittrex(endPoint);
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
