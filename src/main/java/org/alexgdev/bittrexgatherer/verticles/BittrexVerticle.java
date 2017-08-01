package org.alexgdev.bittrexgatherer.verticles;

import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

@Component
public class BittrexVerticle extends AbstractVerticle{
	
	private JsonObject config;
	
	@Autowired
	OrderFillService service;
	
	 @Override
	  public void start() throws Exception {
		 String protocol = "1.5";
		 String connectionToken = "za2pXHz2BKKQDHuKRwip7V7TcxwJ47YVGBCKQWMq9ZglQTi66WlMuXj75RPLArh0S8fEWCLSNyPYIktQIkdSmwyrACMjfDd74J2a5fMypb4yJ1fg";
		 String connectionData = "%5B%7B%22name%22%3A%22corehub%22%7D%5D";
		 String tid ="8";
		 String tradingPair="BTC-ARK";
		 config = new JsonObject()
					.put("protocol", protocol)
					.put("connectionToken", connectionToken)
					.put("tradingPair", tradingPair)
					.put("connectionData", connectionData)
					.put("tid", tid);

		DeploymentOptions options = new DeploymentOptions().setConfig(config);
		vertx.deployVerticle(new BittrexOrderBookVerticle(), options,res -> {
			  if (res.succeeded()) {
				    System.out.println("Deployment id is: " + res.result());
  
			  } else {
				    System.out.println("Deployment failed!");   
			  }
		});
		vertx.deployVerticle(new BittrexPriceVerticle(service), options,res -> {
			  if (res.succeeded()) {
				    System.out.println("Deployment id is: " + res.result());
  
			  } else {
				    System.out.println("Deployment failed!");    
			  }
		});
		vertx.deployVerticle(new BittrexWebsocketVerticle(), options,res -> {
			  if (res.succeeded()) {
				    System.out.println("Deployment id is: " + res.result());
    
			  } else {
				    System.out.println("Deployment failed!");    
			  }
		});
		
		Router router = Router.router(vertx);
		router.route("/eventbus/*").handler(eventBusHandler());
        router.route().failureHandler(errorHandler());
        router.route("/*").handler(StaticHandler.create("static").setCachingEnabled(false));
        router.route().handler(FaviconHandler.create("static/favicon.ico"));

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080));
	 }
	 
	 private ErrorHandler errorHandler() {
		    return ErrorHandler.create();
	 }
	 
	 private SockJSHandler eventBusHandler() {
		 	SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
		    BridgeOptions boptions = new BridgeOptions()
		            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.UPDATE_ORDERBOOK+":[A-Z]+-[A-Z]+"))
		            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.ORDERBOOK_READY+":[A-Z]+-[A-Z]+"))
		    		.addInboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.GET_ORDERBOOK+":[A-Z]+-[A-Z]+"))
		    		.addInboundPermitted(new PermittedOptions().setAddressRegex("getMovingAverage:[A-Z]+-[A-Z]+"));
		    return SockJSHandler.create(vertx, options).bridge(boptions, event -> {
		         if (event.type() == BridgeEventType.SOCKET_CREATED) {
		            System.out.println("A socket was created!");
		         } else if (event.type() == BridgeEventType.SOCKET_CLOSED){
		        	 System.out.println("A socket was closed!");
		         }
		        event.complete(true);
		    });
	 }

}
