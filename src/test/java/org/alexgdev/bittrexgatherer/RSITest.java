package org.alexgdev.bittrexgatherer;

import static org.junit.Assert.*;

import org.alexgdev.bittrexgatherer.util.RSI;
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
		rsi.add(44.3389);
		assertEquals(100.0, rsi.getRSI(), 0.00001);
		rsi.add(44.0902);
		rsi.add(44.1497);
		rsi.add(43.6124);
		rsi.add(44.3278);
		rsi.add(44.8264);
		rsi.add(45.0955);
		rsi.add(45.4245);
		rsi.add(45.8433);
		rsi.add(46.0826);
		rsi.add(45.8931);
		rsi.add(46.0328);
		rsi.add(45.614);
		rsi.add(46.282);
		rsi.add(46.282);
		assertEquals(70.53, rsi.getRSI(), 0.01);
		rsi.add(46.0028);
		assertEquals(66.32, rsi.getRSI(), 0.01);
	}

}
