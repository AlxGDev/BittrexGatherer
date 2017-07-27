package org.alexgdev.bittrexgatherer.verticles;

import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

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
	 }

}
