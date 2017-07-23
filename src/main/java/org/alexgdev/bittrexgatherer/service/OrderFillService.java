package org.alexgdev.bittrexgatherer.service;

import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;
import org.alexgdev.bittrexgatherer.entities.OrderFill;
import org.alexgdev.bittrexgatherer.entities.OrderFillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderFillService {
	
	@Autowired
	private OrderFillRepository repo;
	
	public void save(OrderFillDTO dto, String tradingPair, String exchange){
		this.repo.save(new OrderFill(dto, tradingPair, exchange));
	}

}
