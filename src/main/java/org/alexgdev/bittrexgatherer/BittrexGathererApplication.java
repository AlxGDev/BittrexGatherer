package org.alexgdev.bittrexgatherer;




import org.alexgdev.bittrexgatherer.verticles.BittrexVerticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;


@SpringBootApplication
public class BittrexGathererApplication implements CommandLineRunner{
	
	@Autowired
	private BittrexVerticle baseVerticle;
	
	private Vertx vertx;


	
	
    public static void main(String[] args) {
    	SpringApplication springApplication = new SpringApplicationBuilder()
				.sources(BittrexGathererApplication.class)
				.web(false)
				.build();
	
		springApplication.run();
        
    }

	
	

	@Override
	public void run(String... arg0) throws Exception {
		vertx = Vertx.vertx();
		
		vertx.deployVerticle(baseVerticle,res -> {
			  if (res.succeeded()) {
				  System.out.println("Deployed Base Verticle");
				  String baseVerticleId = res.result();
				  SharedData sd = vertx.sharedData();
				  LocalMap<String, String> map1 = sd.getLocalMap("verticleIDs");
				  map1.put("baseVerticleId", baseVerticleId);
				  
			  } else {
				  System.out.println("Deployment failed!");
				  
			  }
		});
	}
    
    

	
}
