package org.alexgdev.bittrexgatherer.dto;

public class MessageDTO {
	private String message;
	private MessageType type;
	private Object data;
	  
	public MessageDTO() {
	   super();
	}
	  
	public MessageDTO(MessageType type, String message, Object data) {
		super();
	    this.message = message;
	    this.type = type;
	    this.data = data;
	}

	public String getMessage() {
	    return message;
	}
	  
	public void setMessage(String message) {
	    this.message = message;
	}
	
	public Object getData() {
	    return data;
	}
	  
	public void setData(Object data) {
	    this.data = data;
	}
	  
	public MessageType getType() {
	    return type;
	}
	  
	public void setType(MessageType type) {
	    this.type = type;
	}	

}
