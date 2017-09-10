package org.alexgdev.bittrexgatherer;




import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
				  
				  String url = "http://localhost:8080";

			      if(Desktop.isDesktopSupported()){
			            Desktop desktop = Desktop.getDesktop();
			            try {
			                desktop.browse(new URI(url));
			            } catch (IOException | URISyntaxException e) {
			                // TODO Auto-generated catch block
			                e.printStackTrace();
			            }
			      }else{
			            Runtime rt = Runtime.getRuntime();
			            String os = System.getProperty("os.name").toLowerCase();
			            try {
			            	if(os.indexOf("win") >= 0){
			            		rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
			            	} else if(os.indexOf("mac") >= 0){
			            		rt.exec("open " + url);
			            	} else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {

			        	        // Do a best guess on unix until we get a platform independent way
			        	        // Build a list of browsers to try, in this order.
			        	        String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
			        	       			             "netscape","opera","links","lynx"};
			        	        	
			        	        // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
			        	        StringBuffer cmd = new StringBuffer();
			        	        for (int i=0; i<browsers.length; i++)
			        	            cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
			        	        	
			        	        rt.exec(new String[] { "sh", "-c", cmd.toString() });

			                   }
			                
			            } catch (IOException e) {
			                // TODO Auto-generated catch block
			                e.printStackTrace();
			            }
			      }
				  
			  } else {
				  System.out.println("Deployment failed!");
				  
			  }
		});
	}
    
    

	
}
