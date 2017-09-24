package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alexgdev.bittrexgatherer.dto.IndicatorDTO;
import org.alexgdev.bittrexgatherer.dto.MovingAverageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.indicators.SimpleMovingAverage;
import org.alexgdev.bittrexgatherer.indicators.BollingerBands;
import org.alexgdev.bittrexgatherer.indicators.RSI;
import org.alexgdev.bittrexgatherer.indicators.VolumeWeightedMovingAverage;
import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.alexgdev.bittrexgatherer.util.MarketTick;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String UPDATE_INDICATORS = "UPDATEINDICATORS";
	
	private String tradingPair;
	private String updateIndicatorsMessage;
	
	//5min tick
	private Long timerID1;
	private int timeInterval1;
	private MarketTick tick1;
	private IndicatorDTO indicators1;
	
	//10min tick
	/* private Long timerID2;
	private int timeInterval2;
	private MarketTick tick2;
	private SimpleMovingAverage ma_2;*/
	
	private Map<String, Long> timers = new HashMap<>();
	
	
	private MovingAverageDTO dto;
	
	
	private OrderFillService service;
	
	public BittrexPriceVerticle(OrderFillService service){
		super();
		this.service = service;
		this.indicators1 = new IndicatorDTO();
		SimpleMovingAverage ma_1 = new SimpleMovingAverage(10, "ma_1");
		BollingerBands bb_1 = new BollingerBands(ma_1, 2);
		VolumeWeightedMovingAverage vwma_1 = new VolumeWeightedMovingAverage(10, "vwma_1");
		RSI rsi_1 = new RSI(14, "rsi_1");
		tick1 = new MarketTick();
		
		indicators1.setBb(bb_1);
		indicators1.setRsi(rsi_1);
		indicators1.setSma(ma_1);
		indicators1.setVwma(vwma_1);
		timeInterval1 = 5*60*1000;
		
	}
	
	@Override
    public void start() throws Exception {
        super.start();
		tradingPair = config().getString("tradingPair");
		updateIndicatorsMessage = UPDATE_INDICATORS+":"+tradingPair;
		dto = new MovingAverageDTO();
		dto.setTradingPair(tradingPair);

        vertx.eventBus()
                .<JsonObject>consumer(HANDLE_FILLS+":"+tradingPair)
                .handler(processFills());
        timers.put("timer1",  vertx.setTimer(timeInterval1, id -> {
	    	  calculateIndicators(indicators1, tick1, timeInterval1, "timer1");
	    }));
    }
	
	private void calculateIndicators(IndicatorDTO indicators, MarketTick tick, int interval, String timerid){
		vertx.<String>executeBlocking(future -> {
	            System.out.println(tick);
				if(tick.getRate() > 0.0){
					indicators.setCurrentPrice(tick.getRate());
					indicators.getBb().add(tick);
					indicators.getRsi().add(tick);
					indicators.getSma().add(tick);
					indicators.getVwma().add(tick);
	               //SharedData sd = vertx.sharedData();
	               //LocalMap<String, Double> map1 = sd.getLocalMap(tradingPair+" Price");     
				}
            	
				tick.clearPeriod();
            	
            	
            	
            	System.out.println("Indicators: "+ indicators.convertToJson().encode());
            	future.complete();
            	
            	
            
			
        }, result -> {
            if (result.succeeded()) {
                //System.out.println("Done processing price: "+dto);
            	vertx.eventBus().<JsonObject>publish(updateIndicatorsMessage, indicators.convertToJson());
            	timers.put(timerid,vertx.setTimer(interval, id -> {
            		calculateIndicators(indicators, tick, interval, timerid);
              	}));
            } else {
            	System.out.println("Failed calculating Indicators");
            	result.cause().printStackTrace();
            	timers.put(timerid,vertx.setTimer(interval, id -> {
            		calculateIndicators(indicators, tick, interval, timerid);
            	}));
            }
        });

	}
	
	
	private Handler<Message<JsonObject>> processFills() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            OrderBookUpdate update = msg.body().mapTo(OrderBookUpdate.class);
	            
				for(OrderFillDTO fill : update.getFills()){
					tick1.add(fill);
					//tick2.add(fill);
					//service.save(fill, tradingPair, "bittrex");
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
