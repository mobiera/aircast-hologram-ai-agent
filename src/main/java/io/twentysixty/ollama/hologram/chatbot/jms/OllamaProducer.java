package io.twentysixty.ollama.hologram.chatbot.jms;


import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.twentysixty.sa.client.jms.AbstractProducer;
import io.twentysixty.sa.client.util.JsonUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;


@ApplicationScoped
public class OllamaProducer extends AbstractProducer<UUID> {

	@Inject
    ConnectionFactory _connectionFactory;

	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ex.delay")
	Long _exDelay;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollama.queue.name")
	String _queueName;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollama.producer.threads")
	Integer _threads;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.debug")
	Boolean _debug;
	
	
	
	private static final Logger logger = Logger.getLogger(OllamaProducer.class);
	
    
    void onStart(@Observes StartupEvent ev) {
    	logger.info("onStart: BeProducer");
    	
    	this.setExDelay(_exDelay);
		this.setDebug(_debug);
		this.setQueueName(_queueName);
		this.setThreads(_threads);
		this.setConnectionFactory(_connectionFactory);

    	this.setProducerCount(_threads);
    	
    }

    void onStop(@Observes ShutdownEvent ev) {
    	
    	logger.info("onStop: BeProducer");
    }
 
 
    @Override
    public void sendMessage(UUID message) throws Exception {
    	if(_debug) {
    		logger.info("sendMessage: " + JsonUtil.serialize(message, false));
    	}
    	this.spool(message, 0);
    }
    
    
    

}