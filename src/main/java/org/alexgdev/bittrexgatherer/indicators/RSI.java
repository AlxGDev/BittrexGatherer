package org.alexgdev.bittrexgatherer.indicators;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

import lombok.Data;

@Data
public class RSI {
	
	private String id;
	private int period;
	private Double lastPrice;
	private Double avgGain;
	private Double avgLoss;
	private Double sumGain;
	private Double sumLoss;
	private int countCollected;
	public RSI(int period, String id) {
		this.id = id;
		this.sumGain = 0.0;
		this.lastPrice = 0.0;
		this.sumLoss = 0.0;
		this.avgGain = 0.0;
		this.avgLoss = 0.0;
		this.lastPrice = null;
        this.period = period;
        this.countCollected = 0;
    }
	
	public void add(PriceEvent e){
		Double currentPrice = e.getRate();
		if(lastPrice == null){
			lastPrice = currentPrice;
			countCollected = 1;
		} else if(countCollected != period+1) {
			if(currentPrice - lastPrice > 0){
				sumGain += (currentPrice - lastPrice);	
			} else {
				sumLoss += (-1*(currentPrice - lastPrice));	
			}
			avgGain = sumGain / countCollected;
			avgLoss = sumLoss / countCollected;
			lastPrice = currentPrice;
			countCollected++;
		} else {
			if(currentPrice - lastPrice > 0){
				avgGain = (((period-1)*avgGain) + (currentPrice - lastPrice))/period;
				avgLoss = (((period-1)*avgLoss))/period;
			} else {
				avgGain = (((period-1)*avgGain))/period;
				avgLoss = (((period-1)*avgLoss) +(-1*(currentPrice - lastPrice)))/period;	
			}
			lastPrice = currentPrice;
		}
	}
	
	public Double getRSI(){
		if(avgLoss == 0){
			return 100.0;
		} else if (avgGain == 0){
			return 0.0;
		}
		return 100 - (100/(1+(avgGain/avgLoss)));
		/*if(countCollected <= period){
			//not enough data
			return -1.0;
		} else {
			return 100 - (100/(1+(avgGain/avgLoss)));
		}*/
	}
	

}
