package org.alexgdev.bittrexgatherer.verticles;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.alexgdev.bittrexgatherer.dto.MessageDTO;
import org.alexgdev.bittrexgatherer.dto.MessageType;
import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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
	
	public static final String REDEPLOYBITTREXVERTICLES = "REDEPLOYBITTREXVERTICLES";
	
	
	private JsonObject config;
	private String o_id;
	private String p_id;
	private String ws_id;
	
	private DeploymentOptions options;
	
	private WebClient restclient;
	
	@Autowired
	OrderFillService service;
	
	 @Override
	  public void start() throws Exception {
		 String tradingPair="BTC-ARK";
		 config = new JsonObject()
					.put("tradingPair", tradingPair);
		 
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
		WebClientOptions webclientoptions = new WebClientOptions().setUserAgent(userAgent);
		webclientoptions.setKeepAlive(false);
		restclient = WebClient.create(vertx, webclientoptions);
		
		vertx.eventBus()
    	.<String>consumer(REDEPLOYBITTREXVERTICLES)
    	.handler(handleVerticleRedeploy());
		
		options = new DeploymentOptions();
		options.setConfig(config);
		redeployVerticles(options);
		
		
		Router router = Router.router(vertx);
		router.route("/eventbus/*").handler(eventBusHandler());
		router.get("/api/tradingpairs")
        .handler(this::handleGetTradingPairs);
        router.route().failureHandler(errorHandler());
        router.route("/*").handler(StaticHandler.create("static").setCachingEnabled(false));
        router.route().handler(FaviconHandler.create("static/favicon.ico"));

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080));
       
        
	 }
	 
	 //need to route through server because bittrex has cors enabled, can't do it in browser
	 public void handleGetTradingPairs(RoutingContext context){
	    
		 HttpRequest<Buffer> request = restclient.get(443, "www.bittrex.com", "/api/v1.1/public/getmarkets");		 
			  
		 request.ssl(true).send(ar -> {
			  
			  if (ar.succeeded()) {
				  HttpResponse<Buffer> response = ar.result();
				  if(response.statusCode() == 200){
					  JsonObject body = response.bodyAsJsonObject();
					  context.response()
	                  .putHeader("content-type", "application/json")
	                  .setStatusCode(200)
	                  .end(body.encode());
					  
					  
				  } else {
					  System.err.println(response.bodyAsString());
					  MessageDTO dto = new MessageDTO(MessageType.ERROR, "Could not retrieve tradingpairs: "+response.statusCode(), null);
		              context.response()
		                	.putHeader("content-type", "application/json")
		                    .setStatusCode(response.statusCode())
		                    .end(JsonObject.mapFrom(dto).encodePrettily());
				  }
				  	  
		      
			  } else {
				  MessageDTO dto = new MessageDTO(MessageType.ERROR, "Could not retrieve tradingpairs: "+ar.cause().getMessage(), null);
	              context.response()
	              	.putHeader("content-type", "application/json")
	              	.setStatusCode(500)
	              	.end(JsonObject.mapFrom(dto).encodePrettily());
			  }
		  });		 
		  
	}
	 
	 private Handler<Message<String>> handleVerticleRedeploy(){
			return msg -> {
				System.out.println(msg);
				config.put("tradingPair", msg.body());
				options.setConfig(config);
				redeployVerticles(options);
				msg.reply("redeploying");
			};
	}
	 
	private void redeployVerticles(DeploymentOptions options){
		Future<String> future1 = undeployVerticles();
		future1.compose(s1 -> {
			  
			Future<String> future2 = deployVerticles(options);
			future2.setHandler(handler -> {
				if (handler.succeeded()) {
				
					System.out.println("Redployment successful!");
					
				} else {
					System.err.println("Redeployment failed: " + handler.cause().getMessage());
					
				}
			});
		}, Future.future().setHandler(handler -> {
			System.err.println("Redeployment failed: " + handler.cause().getMessage());
			
		}));
	}
	 
	private Future<String> deployVerticles(DeploymentOptions options){
		Future<String> future = Future.future();
		
		
		Future<String> future1 = deployOrderBookVerticle(options);
		
		future1.compose(s1 -> {
			  
			Future<String> future2 = deployPriceVerticle(options);
			// Define future2 composition
			future2.compose(s2 -> {
					// Because the future3 is the last, we define here a handler
					Future<String> future3 = deployWebSocketVerticle(options);
					  future3.setHandler(handler -> {
							if (handler.succeeded()) {
							
								System.out.println("Deployment successful!");
								future.complete("Deployment successful!");
							} else {
								System.err.println("Deployment failed: " + handler.cause().getMessage());
								future.fail(handler.cause().getMessage());
							}
						});
				} , Future.future().setHandler(handler -> {
					System.err.println("Deployment failed: " + handler.cause().getMessage());
					future.fail(handler.cause().getMessage());
				}));
		}, Future.future().setHandler(handler -> {
			System.err.println("Deployment failed: " + handler.cause().getMessage());
			future.fail(handler.cause().getMessage());
		}));
		
		return future;
		
	}
	
	private Future<String> undeployVerticles(){
		Future<String> future = Future.future();
		
		Future<String> future1 = undeployOrderBookVerticle();
		
		future1.compose(s1 -> {
			  
			Future<String> future2 = undeployPriceVerticle();
			// Define future2 composition
			future2.compose(s2 -> {
					// Because the future3 is the last, we define here a handler
					Future<String> future3 = undeployWebSocketVerticle();
					  future3.setHandler(handler -> {
							if (handler.succeeded()) {
							
								System.out.println("Undeployment successful!");
								future.complete("Undeployment successful!");
							} else {
								System.err.println("Undeployment failed: " + handler.cause().getMessage());
								future.fail(handler.cause().getMessage());
							}
						});
				} , Future.future().setHandler(handler -> {
					System.err.println("Undeployment failed: " + handler.cause().getMessage());
					future.fail(handler.cause().getMessage());
				}));
		}, Future.future().setHandler(handler -> {
			System.err.println("Undeployment failed: " + handler.cause().getMessage());
			future.fail(handler.cause().getMessage());
		}));
		return future;
	}
	
	private Future<String> deployOrderBookVerticle(DeploymentOptions options) {
		Future<String> future = Future.future();
		vertx.deployVerticle(new BittrexOrderBookVerticle(), options, res -> {
			  if (res.succeeded()) {
				  o_id = res.result();
				  System.out.println("OrderBookVerticle Deployment id is: " + res.result());
				  future.complete();

			  } else {
				  System.err.println("OrderBookVerticle failed: " + res.cause().getMessage()); 
				  future.fail(res.cause());
				     
			  }
		});
		
		return future;
	}
	
	private Future<String> undeployOrderBookVerticle() {
		Future<String> future = Future.future();
		if(o_id == null){
			future.complete();
		} else {
			vertx.undeploy(o_id, res -> {
				  if (res.succeeded()) {
						  o_id = null;
						  System.out.println("OrderBookVerticle undeployed");
						  future.complete();
					  } else {
						  System.err.println("OrderBookVerticle Undeployment failed: " + res.cause().getMessage()); 
						  future.fail(res.cause());
					  }
			});
		}
		return future;
	}
	
	private Future<String> deployPriceVerticle(DeploymentOptions options) {
		Future<String> future = Future.future();
		vertx.deployVerticle(new BittrexPriceVerticle(service), options, res -> {
			  if (res.succeeded()) {
				  p_id = res.result();
				  System.out.println("PriceVerticle Deployment id is: " + res.result());
				  future.complete();

			  } else {
				  System.err.println("PriceVerticle Deployment failed: " + res.cause().getMessage()); 
				  future.fail(res.cause());
				     
			  }
		});
		
		return future;
	}
	
	private Future<String> undeployPriceVerticle() {
		Future<String> future = Future.future();
		if(p_id == null){
			future.complete();
		} else {
			vertx.undeploy(p_id, res -> {
				  if (res.succeeded()) {
						  p_id = null;
						  System.out.println("PriceVerticle undeployed");
						  future.complete();
					  } else {
						  System.err.println("PriceVerticle Undeployment failed: " + res.cause().getMessage()); 
						  future.fail(res.cause());
					  }
			});
		}
		return future;
	}
	
	private Future<String> deployWebSocketVerticle(DeploymentOptions options) {
		
		Future<String> future = Future.future();
		vertx.deployVerticle(new BittrexWebsocketVerticle(), options, res -> {
			  if (res.succeeded()) {
				  ws_id = res.result();
				  System.out.println("WebsocketVerticle Deployment id is: " + res.result());
				  future.complete();

			  } else {
				  System.err.println("WebsocketVerticle Deployment failed: " + res.cause().getMessage()); 
				  future.fail(res.cause());
				     
			  }
		});
		
		return future;
	}
	
	private Future<String> undeployWebSocketVerticle() {
		Future<String> future = Future.future();
		if(ws_id == null){
			future.complete();
		} else {
			vertx.undeploy(ws_id, res -> {
				  if (res.succeeded()) {
						  ws_id = null;
						  System.out.println("WebsocketVerticle undeployed");
						  future.complete();
					  } else {
						  System.err.println("WebsocketVerticle Undeployment failed: " + res.cause().getMessage()); 
						  future.fail(res.cause());
					  }
			});
		}
		return future;
	}
	
	 
	 
	 private ErrorHandler errorHandler() {
		    return ErrorHandler.create();
	 }
	 
	 private SockJSHandler eventBusHandler() {
		 	SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
		    BridgeOptions boptions = new BridgeOptions()
		            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.UPDATE_ORDERBOOK+":[A-Z]+-[A-Z]+"))
		            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BittrexPriceVerticle.UPDATE_INDICATORS+":[A-Z]+-[A-Z]+"))
		            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.ORDERBOOK_READY+":[A-Z]+-[A-Z]+"))
		    		.addInboundPermitted(new PermittedOptions().setAddressRegex(BittrexOrderBookVerticle.GET_ORDERBOOK+":[A-Z]+-[A-Z]+"))
		    		.addInboundPermitted(new PermittedOptions().setAddressRegex("getMovingAverage:[A-Z]+-[A-Z]+"))
		    		.addInboundPermitted(new PermittedOptions().setAddressRegex("REDEPLOYBITTREXVERTICLES"));
		    return SockJSHandler.create(vertx, options).bridge(boptions, event -> {
		         if (event.type() == BridgeEventType.SOCKET_CREATED) {
		            System.out.println("A socket was created!");
		         } else if (event.type() == BridgeEventType.SOCKET_CLOSED){
		        	 System.out.println("A socket was closed!");
		         } else if (event.type() == BridgeEventType.UNREGISTER || event.type() == BridgeEventType.REGISTER){
		        	 System.out.println(event.getRawMessage().encode());
		         }
		        event.complete(true);
		    });
	 }

}
