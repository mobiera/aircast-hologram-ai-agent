package io.twentysixty.langchain.hologram.chatbot.jms;

import java.io.Serializable;
import java.util.UUID;

public class OllamaMsg implements Serializable {

	private UUID uuid;
	private String content;
	private String model;
	
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	
}
