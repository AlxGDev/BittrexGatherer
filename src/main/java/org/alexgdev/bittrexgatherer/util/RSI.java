package org.alexgdev.bittrexgatherer.util;

public class RSI {
	
	private int period;
	private Double lastPrice;
	private Double avgGain;
	private Double avgLoss;
	private Double sumGain;
	private Double sumLoss;
	private int countCollected;
	public RSI(int period) {
		this.sumGain = 0.0;
		this.lastPrice = 0.0;
		this.sumLoss = 0.0;
		this.avgGain = 0.0;
		this.avgLoss = 0.0;
		this.lastPrice = null;
        this.period = period;
        this.countCollected = 0;
    }
	
	public void add(Double currentPrice){
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
	
	public double getRSI(){
		if(countCollected <= period){
			//not enough data
			return -1.0;
		} else {
			return 100 - (100/(1+(avgGain/avgLoss)));
		}
	}
	

}
