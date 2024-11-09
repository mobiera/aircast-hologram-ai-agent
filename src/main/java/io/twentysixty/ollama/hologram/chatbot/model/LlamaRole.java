package io.twentysixty.ollama.hologram.chatbot.model;


public enum LlamaRole {
	
	SYSTEM(0, "system"), 
	USER(1, "user"), 
	ASSISTANT(2, "assistant"),
	TOOL(3, "tool"),;
	
	private String label;
	
	private LlamaRole(Integer index, String label){
		this.index = index;
		this.label = label;
		
	}

	private Integer index;

	public Integer getIndex(){
		return this.index;
	}

	public static LlamaRole getEnum(Integer index){
		if (index == null)
	return null;

		switch(index){
			case 0: return SYSTEM;
			case 1: return USER;
			case 2: return ASSISTANT;
			case 3: return TOOL;
					
			default: return null;
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	

	public void setIndex(Integer index) {
		this.index = index;
	}

	
	public String getName() {
		return this.toString();
	}
	
	
	public String getValue() {
		return this.toString();
	}

}
