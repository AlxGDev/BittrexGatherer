package org.alexgdev.bittrexgatherer;

import static org.junit.Assert.*;

import org.alexgdev.bittrexgatherer.indicators.RSI;
import org.alexgdev.bittrexgatherer.util.MarketTick;
import org.junit.Before;
import org.junit.Test;

public class RSITest {

	private RSI rsi;
	@Before
	public void setUp() throws Exception {
		rsi = new RSI(14, "rsi");
	}

	@Test
	public void test() {
		assertEquals(100.0, rsi.getRSI(), 0.00001);
		rsi.add(new MarketTick(44.3389));
		assertEquals(100.0, rsi.getRSI(), 0.00001);
		rsi.add(new MarketTick(44.0902));
		rsi.add(new MarketTick(44.1497));
		rsi.add(new MarketTick(43.6124));
		rsi.add(new MarketTick(44.3278));
		rsi.add(new MarketTick(44.8264));
		rsi.add(new MarketTick(45.0955));
		rsi.add(new MarketTick(45.4245));
		rsi.add(new MarketTick(45.8433));
		rsi.add(new MarketTick(46.0826));
		rsi.add(new MarketTick(45.8931));
		rsi.add(new MarketTick(46.0328));
		rsi.add(new MarketTick(45.614));
		rsi.add(new MarketTick(46.282));
		rsi.add(new MarketTick(46.282));
		assertEquals(70.53, rsi.getRSI(), 0.01);
		rsi.add(new MarketTick(46.0028));
		assertEquals(66.32, rsi.getRSI(), 0.01); 
	}

}
