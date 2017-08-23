package org.alexgdev.bittrexgatherer.indicators;

import org.alexgdev.bittrexgatherer.util.PriceEvent;

public interface MovingStatistic {
	
	public void add(PriceEvent e);
	public Double getAverage(); 
    public Double getVariance();
    public Double getStdDev(); 
    public String getId();

}
