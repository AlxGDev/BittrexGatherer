package org.alexgdev.bittrexgatherer.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.alexgdev.bittrexgatherer.dto.OrderFillDTO;

import lombok.Data;

@Data
@Entity
public class OrderFill {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
	private Long id;
	
	@NotNull
	private String tradingPair;
	
	@NotNull
	private String exchange;
	
	@NotNull
	private String orderType;
	
	@NotNull
	private double rate;
	
	@NotNull
	private double quantity;
	
	@NotNull
	private LocalDateTime timeStamp;
	
	public OrderFill(){};
	
	public OrderFill(OrderFillDTO dto, String tradingPair, String exchange){
		this.orderType = dto.getOrderType();
		this.tradingPair = tradingPair;
		this.exchange = exchange;
		this.rate = dto.getRate();
		this.quantity = dto.getQuantity();
		this.timeStamp = LocalDateTime.parse(dto.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME);
	}

}
