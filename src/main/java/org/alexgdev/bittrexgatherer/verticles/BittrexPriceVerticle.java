package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alexgdev.bittrexgatherer.dto.IndicatorDTO;
import org.alexgdev.bittrexgatherer.dto.MarketTickDTO;
import org.alexgdev.bittrexgatherer.dto.MovingAverageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.dto.TickUpdate;
import org.alexgdev.bittrexgatherer.indicators.SimpleMovingAverage;
import org.alexgdev.bittrexgatherer.indicators.BollingerBands;
import org.alexgdev.bittrexgatherer.indicators.RSI;
import org.alexgdev.bittrexgatherer.indicators.VolumeWeightedMovingAverage;
import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.alexgdev.bittrexgatherer.util.MarketTick;
import org.alexgdev.bittrexgatherer.util.MessageDefinitions;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	
	
	private String tradingPair;
	private String updateIndicatorsMessage;
	
	//5min tick
	private Long indicatorTimer;
	private int timerInterval;
	private MarketTick currentTick;
	private IndicatorDTO indicators;
	private CircularFifoQueue<MarketTick> cachedTicks;
	
	
	private MovingAverageDTO dto;
	
	
	private OrderFillService service;
	
	public BittrexPriceVerticle(OrderFillService service){
		super();
		service = service;
		indicators = new IndicatorDTO();
		cachedTicks = new CircularFifoQueue<MarketTick>(20);
		SimpleMovingAverage ma_1 = new SimpleMovingAverage(20, "ma_1");
		BollingerBands bb_1 = new BollingerBands(ma_1, 2);
		VolumeWeightedMovingAverage vwma_1 = new VolumeWeightedMovingAverage(20, "vwma_1");
		RSI rsi_1 = new RSI(14, "rsi_1");
		currentTick = new MarketTick();
		
		indicators.setBb(bb_1);
		indicators.setRsi(rsi_1);
		indicators.setSma(ma_1);
		indicators.setVwma(vwma_1);
		timerInterval = 5*60*1000;
		
	}
	
	@Override
    public void start() throws Exception {
        super.start();
		tradingPair = config().getString("tradingPair");
		updateIndicatorsMessage = MessageDefinitions.UPDATE_INDICATORS+":"+tradingPair;
		dto = new MovingAverageDTO();
		dto.setTradingPair(tradingPair);

        vertx.eventBus()
                .<JsonObject>consumer(MessageDefinitions.HANDLE_FILLS+":"+tradingPair)
                .handler(processFills());
        
        vertx.eventBus()
        	.<JsonObject>consumer(MessageDefinitions.SET_LASTTICKS+":"+tradingPair)
        	.handler(processLastTicks());
        
        indicatorTimer =   vertx.setTimer(timerInterval, id -> {
	    	  calculateIndicators();
	    });
    }
	
	private void calculateIndicators(){
		 System.out.println(currentTick);
		 if(currentTick.getRate() > 0.0){
					indicators.setCurrentPrice(currentTick.getRate());
					indicators.getBb().add(currentTick);
					indicators.getRsi().add(currentTick);
					indicators.getSma().add(currentTick);
					indicators.getVwma().add(currentTick);
					cachedTicks.add(currentTick);
	               //SharedData sd = vertx.sharedData();
	               //LocalMap<String, Double> map1 = sd.getLocalMap(tradingPair+" Price");     
		 }
            	
		 currentTick = new MarketTick();
         
		 System.out.println("Indicators: "+ indicators.convertToJson().encode());
         vertx.eventBus().publish(updateIndicatorsMessage, indicators.convertToJson());
         indicatorTimer = vertx.setTimer(timerInterval, id -> {
        	 calculateIndicators();
         });

	}
	
	private Handler<Message<JsonObject>> processLastTicks() {
        return msg -> {
        	try{
        		
        		vertx.cancelTimer(indicatorTimer);
        		TickUpdate update = msg.body().mapTo(TickUpdate.class);
        		
				for(MarketTickDTO tick : update.getTicks()){
					currentTick.setHigh(tick.getHigh());
					currentTick.setLow(tick.getLow());
					currentTick.setVolume(tick.getVolume());
					
					indicators.setCurrentPrice(currentTick.getRate());
					indicators.getBb().add(currentTick);
					indicators.getRsi().add(currentTick);
					indicators.getSma().add(currentTick);
					indicators.getVwma().add(currentTick);
					
					cachedTicks.add(currentTick);
					currentTick = new MarketTick();
	
				}
				System.out.println(indicators.getBb().getLowerBand()+ " "+indicators.getBb().getMa().getAverage()+" "+indicators.getBb().getUpperBand());
				timerInterval = update.getInterval()*60*1000;
				indicatorTimer = vertx.setTimer(timerInterval, id -> {
		        	 calculateIndicators();
		         });
				vertx.eventBus().publish(updateIndicatorsMessage, indicators.convertToJson());
				
        	} catch(Exception e){
        		e.printStackTrace();
        		System.out.println("Failed processing last ticks");
        	}
        };
    }
	
	private Handler<Message<JsonObject>> processFills() {
        return msg -> {
        	try{
	            OrderBookUpdate update = msg.body().mapTo(OrderBookUpdate.class);
	            
				for(OrderFillDTO fill : update.getFills()){
					currentTick.add(fill);
					
				}
				
        	} catch(Exception e){
        		e.printStackTrace();
        		System.out.println("Failed processing fills");
        	}
        };
    }
	
	
	
	private Handler<Message<JsonObject>> processFillsAndSave() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            OrderBookUpdate update = msg.body().mapTo(OrderBookUpdate.class);
	            
				for(OrderFillDTO fill : update.getFills()){
					currentTick.add(fill);
					service.save(fill, tradingPair, "bittrex");
				}
				future.complete();
        	} catch(Exception e){
        		e.printStackTrace();
        		future.fail(e);
        	}
        }, result -> {
            if (result.succeeded()) {
                //System.out.println("Done processing fills");
            } else {
            	result.cause().printStackTrace();
            	System.out.println("Failed processing fills");
            }
        });
    } 

	

}
