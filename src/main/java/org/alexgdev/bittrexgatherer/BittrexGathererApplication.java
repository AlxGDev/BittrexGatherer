package org.alexgdev.bittrexgatherer;




import org.alexgdev.bittrexgatherer.verticles.BittrexVerticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@SpringBootApplication
public class BittrexGathererApplication extends Application implements CommandLineRunner{
	
	@Autowired
	private BittrexVerticle baseVerticle;
	
	private ConfigurableApplicationContext springContext;
	private SpringApplication springApplication;
	private static Vertx vertx;


	
	
    public static void main(String[] args) {
    	
    	launch(BittrexGathererApplication.class);
        
    }

	@Override
    public void init() throws Exception {
		springApplication = new SpringApplicationBuilder()
				.sources(BittrexGathererApplication.class)
				.web(false)
				.build();
	
		springContext = springApplication.run();
		
       /* FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        root = fxmlLoader.load(); */
    }
	
	@Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hello World");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
    	
    	SharedData sd = vertx.sharedData();

    	LocalMap<String, String> map1 = sd.getLocalMap("verticleIDs");
    	String id = map1.get("baseVerticleId");
    	if(id != null){
    		vertx.undeploy(id,res -> {
  			  if (res.succeeded()) {
  				  
  				  System.out.println("Undeployed ok");
  				  vertx.close();
  				  springContext.stop();
  			  } else {
  				  System.out.println("Undeploy failed!");
  				  
  				  System.exit(-1);
  			  }
    		});
    	} else {
    		vertx.close();
        	springContext.stop();
    	}
    	
    	
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
