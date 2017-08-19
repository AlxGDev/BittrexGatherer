package org.alexgdev.bittrexgatherer.verticles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alexgdev.bittrexgatherer.dto.MovingAverageDTO;
import org.alexgdev.bittrexgatherer.dto.OrderBookUpdate;
import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.service.OrderFillService;
import org.alexgdev.bittrexgatherer.util.MarketTick;
import org.alexgdev.bittrexgatherer.util.MovingAverage;
import org.alexgdev.bittrexgatherer.util.RSI;
import org.alexgdev.bittrexgatherer.util.VolumeWeightedMovingAverage;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class BittrexPriceVerticle extends AbstractVerticle{
	public static final String HANDLE_FILLS = "HANDLEFILLS";
	public static final String GET_MA = "GETMA";
	
	private String tradingPair;
	
	//5min tick
	private Long timerID1;
	private int timeInterval1;
	private MarketTick tick1;
	private MovingAverage ma_1;
	private VolumeWeightedMovingAverage vwma_1;
	private RSI rsi_1;
	
	//10min tick
	private Long timerID2;
	private int timeInterval2;
	private MarketTick tick2;
	private MovingAverage ma_2;
	
	private Map<String, Long> timers = new HashMap<>();
	
	
	private MovingAverageDTO dto;
	
	
	private OrderFillService service;
	
	public BittrexPriceVerticle(OrderFillService service){
		super();
		this.service = service;
		ma_1 = new MovingAverage(10, "ma_1");
		vwma_1 = new VolumeWeightedMovingAverage(10, "vwma_1");
		rsi_1 = new RSI(14, "rsi_1");
		tick1 = new MarketTick();
		timeInterval1 = 5*60*1000;
		
		ma_2 = new MovingAverage(10, "ma_2");
		tick2 = new MarketTick();
		timeInterval2 = 10*60*1000;
	}
	
	@Override
    public void start() throws Exception {
        super.start();
		tradingPair = config().getString("tradingPair");
		
		dto = new MovingAverageDTO();
		dto.setTradingPair(tradingPair);

        vertx.eventBus()
                .<JsonObject>consumer(HANDLE_FILLS+":"+tradingPair)
                .handler(processFills());
        vertx.eventBus()
        	.<String>consumer(GET_MA+":"+tradingPair)
        	.handler(getMovingAverage());
        timers.put("timer1",  vertx.setTimer(timeInterval1, id -> {
	    	  calculateIndicators(tick1, ma_1, vwma_1, rsi_1, timeInterval1, "timer1");
	    }));
    }
	
	private void calculateIndicators(MarketTick tick, MovingAverage ma, VolumeWeightedMovingAverage vwma, RSI rsi, int interval, String timerid){
		vertx.<String>executeBlocking(future -> {
	            System.out.println(tick);
				if(tick.getAverage() > 0.0){
	        	   ma.add(tick.getAverage());
	               rsi.add(tick.getAverage());
	               vwma.add(tick.getAverage(), tick.getVolume());
	               SharedData sd = vertx.sharedData();
	               LocalMap<String, Double> map1 = sd.getLocalMap(tradingPair+" Price");
	               map1.put(ma.getId(), ma.getAverage());
	               map1.put(rsi.getId(), rsi.getRSI());
	               map1.put(vwma.getId(), vwma.getAverage());
				}
            	
            	tick.clearPeriod();
            	
            	
            	
            	System.out.println("MA: "+String.format("%.8f", ma.getAverage()));
            	System.out.println("VWMA: "+String.format("%.8f", vwma.getAverage()));
            	System.out.println("RSI: "+String.format("%.8f", rsi.getRSI()));
            	future.complete();
            	
            	
            
			
        }, result -> {
            if (result.succeeded()) {
                //System.out.println("Done processing price: "+dto);
            	timers.put(timerid,vertx.setTimer(interval, id -> {
            		calculateIndicators(tick, ma, vwma, rsi, interval, timerid);
              	}));
            } else {
            	System.out.println("Failed calculating Indicators");
            	result.cause().printStackTrace();
            	timers.put(timerid,vertx.setTimer(interval, id -> {
            		calculateIndicators(tick, ma, vwma, rsi, interval, timerid);
            	}));
            }
        });

	}
	
	private Handler<Message<String>> getMovingAverage(){
		return msg -> {
			MovingAverageDTO dto = new MovingAverageDTO();
			dto.setTimeStamp(System.currentTimeMillis());
			dto.setTradingPair(tradingPair);
			dto.setMovingAverage(ma_1.getAverage());
			String payload = JsonObject.mapFrom(dto).encode();
			msg.reply(payload);
		};
	}
	
	private Handler<Message<JsonObject>> processFills() {
        return msg -> vertx.<String>executeBlocking(future -> {
        	try{
	            OrderBookUpdate update = msg.body().mapTo(OrderBookUpdate.class);
	            
				for(OrderFillDTO fill : update.getFills()){
					tick1.add(fill);
					tick2.add(fill);
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
