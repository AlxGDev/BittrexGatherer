package org.alexgdev.bittrexgatherer.dto;



import lombok.Data;

@Data
public class MessageDTO {
	private String message;
	private OrderBookUpdate payload;

}
