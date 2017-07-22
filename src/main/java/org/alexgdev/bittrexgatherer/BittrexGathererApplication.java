package org.alexgdev.bittrexgatherer;



import org.alexgdev.bittrexgatherer.verticles.BittrexPriceVerticle;
import org.alexgdev.bittrexgatherer.verticles.BittrexVerticle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@SpringBootApplication
public class BittrexGathererApplication implements CommandLineRunner{
	

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplicationBuilder()
	             							.sources(BittrexGathererApplication.class)
	             							.web(false)
	             							.build();
		springApplication.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		final Vertx vertx = Vertx.vertx();
		String protocol = "1.5";
		String connectionToken = "za2pXHz2BKKQDHuKRwip7V7TcxwJ47YVGBCKQWMq9ZglQTi66WlMuXj75RPLArh0S8fEWCLSNyPYIktQIkdSmwyrACMjfDd74J2a5fMypb4yJ1fg";
		String connectionData = "%5B%7B%22name%22%3A%22corehub%22%7D%5D";
		String tid ="8";
		String tradingPair="BTC-ARK";
		
		deployBittrexVerticle(vertx, protocol, connectionToken, tradingPair, connectionData, tid);
        
		
	}
	
	public void deployBittrexVerticle(Vertx vertx,String protocol, String connectionToken, String tradingPair, String connectionData, String tid){
		JsonObject config = new JsonObject()
							.put("protocol", protocol)
							.put("connectionToken", connectionToken)
							.put("tradingPair", tradingPair)
							.put("connectionData", connectionData)
							.put("tid", tid);
							
		DeploymentOptions options = new DeploymentOptions().setConfig(config);
		vertx.deployVerticle(new BittrexPriceVerticle());
		vertx.deployVerticle(new BittrexVerticle(), options);
	}
}
