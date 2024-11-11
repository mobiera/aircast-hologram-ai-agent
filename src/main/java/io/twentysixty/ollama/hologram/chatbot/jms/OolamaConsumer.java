package io.twentysixty.ollama.hologram.chatbot.jms;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.github.ollama4j.exceptions.RoleNotFoundException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.twentysixty.ollama.hologram.chatbot.model.LlamaRole;
import io.twentysixty.ollama.hologram.chatbot.svc.OllamaService;
import io.twentysixty.sa.client.jms.AbstractConsumer;
import io.twentysixty.sa.client.jms.ConsumerInterface;
import io.twentysixty.sa.client.model.message.TextMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;

@ApplicationScoped
public class OolamaConsumer extends AbstractConsumer<OllamaMsg> implements ConsumerInterface<OllamaMsg> {

	@Inject
	OllamaService service;
	
	@Inject
	MtProducer mtProducer;
	

	@Inject
    ConnectionFactory _connectionFactory;

	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ex.delay")
	Long _exDelay;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollama.queue.name")
	String _queueName;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollama.consumer.threads")
	Integer _threads;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.debug")
	Boolean _debug;
	
	
	private static final Logger logger = Logger.getLogger(OolamaConsumer.class);

	
	
	void onStart(@Observes StartupEvent ev) {
    	
		logger.info("onStart: BeConsumer queueName: " + _queueName);
		
		this.setExDelay(_exDelay);
		this.setDebug(_debug);
		this.setQueueName(_queueName);
		this.setThreads(_threads);
		this.setConnectionFactory(_connectionFactory);
		super._onStart();
		
    }

    void onStop(@Observes ShutdownEvent ev) {
    	
    	logger.info("onStop: BeConsumer");
		
    	
    	super._onStop();
    	
    }
	
    @Override
	public void receiveMessage(OllamaMsg msg) throws Exception {
		
    	if (msg.getModel() != null) {
    		List<OllamaChatMessage> messages = service.getMessagesFromHistory(msg.getUuid());
        	
        	logger.info("receive message\n");
        	
        	int i = 0;
        	for (OllamaChatMessage m: messages) {
        		logger.info(i + " " + m.getRole() + " " + m.getContent());
        		i++;
        	}
        	
        	OllamaChatMessage m = new OllamaChatMessage();
    		
    		m.setContent(msg.getContent());
    		m.setRole(OllamaChatMessageRole.USER);
    		messages.add(m);
    		String response = null;
    		
    		try {
    			response = service.getChatResponse(messages, msg.getModel()); 	
    		} catch (Exception e) {
    			response = e.toString();
    		}
        	
    		service.historize(msg.getUuid(), LlamaRole.USER, msg.getContent());
        	service.historize(msg.getUuid(), LlamaRole.ASSISTANT, response);
        	TextMessage tm = TextMessage.build(msg.getUuid(), null, response);
        	mtProducer.sendMessage(tm);
    	}
    	
		
	}

	
}
