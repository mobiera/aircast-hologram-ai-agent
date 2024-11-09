package io.twentysixty.ollama.hologram.chatbot.svc;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.RoleNotFoundException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.response.LibraryModelTag;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.types.OllamaModelType;
import io.twentysixty.ollama.hologram.chatbot.jms.OllamaProducer;
import io.twentysixty.ollama.hologram.chatbot.model.History;
import io.twentysixty.ollama.hologram.chatbot.model.LlamaRole;
import io.twentysixty.ollama.hologram.chatbot.model.Session;
import io.twentysixty.ollama.hologram.chatbot.res.c.MediaResource;
import io.twentysixty.sa.res.c.CredentialTypeResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;



@ApplicationScoped
public class OllamaService {

	private static Logger logger = Logger.getLogger(OllamaService.class);

	@Inject EntityManager em;
	
	@RestClient
	@Inject MediaResource mediaResource;
	
	
	@Inject OllamaProducer mtProducer;
	
	@RestClient
	@Inject CredentialTypeResource credentialTypeResource;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.debug")
	Boolean debug;
	
	@ConfigProperty(name = "io.twentysixty.ollama.hologram.chatbot.ollamaserver.url")
	String serviceUrl;
	
	@ConfigProperty(name = "io.twentysixty.ollama.hologram.chatbot.ollamaserver.maxhistorysize")
	Integer maxHistorySize;
	
	@ConfigProperty(name = "io.twentysixty.ollama.hologram.chatbot.ollamaserver.timeoutseconds")
	Integer timeout;
	
	
	public List<OllamaChatMessage> getMessagesFromHistory(UUID connectionId) {
		
		Session s = em.find(Session.class, connectionId);
		List<History> histories = this.getHistory(s);
		
		List<OllamaChatMessage> messages = new ArrayList<OllamaChatMessage>(histories.size());
		
		for (int i=histories.size()-1;i>=0;i--) {
			History h = histories.get(i);
			OllamaChatMessage m = new OllamaChatMessage();
			OllamaChatMessageRole role;
			try {
				
				/*switch (h.getRole()) {
					
					case ASSISTANT: {
						
					}
					case USER: {
						
					}
					case TOOL: {
						
					}
					default:
					case SYSTEM: {
						
					}
						
				}*/
				
				role = OllamaChatMessageRole.getRole(h.getRole().toString().toLowerCase());
			} catch (RoleNotFoundException e) {
				role = OllamaChatMessageRole.SYSTEM;
			}
			m.setContent(h.getData());
			m.setRole(role);
			messages.add(m);
		}
		
		return messages;
	}
	
	public String getChatResponse(List<OllamaChatMessage> messages) throws OllamaBaseException, IOException, InterruptedException {
		
		this.checkModels();
		
		OllamaAPI ollamaAPI = new OllamaAPI(serviceUrl);
		ollamaAPI.setRequestTimeoutSeconds(timeout);

        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance("llama3.2:1b");
		OllamaChatRequest requestModel = builder.withMessages(messages).build();
		OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

		return chatResult.getResponse();
	}
 	
	public List<History> getHistory(Session session) {
		Query q = em.createNamedQuery("History.find");
		q.setParameter("session", session);
		q.setMaxResults(maxHistorySize);
		return q.getResultList();
	}
	
	@Transactional
	public void historize(UUID connectionId, LlamaRole role, String data) {
		History h = new History();
		h.setData(data);
		h.setRole(role);
		h.setTs(Instant.now());
		h.setHistoryId(UUID.randomUUID());
		Session s = em.find(Session.class, connectionId);
		h.setSession(s);
		em.persist(h);
	}
	
	private void checkModels() {
		OllamaAPI ollamaAPI = new OllamaAPI(serviceUrl);
		ollamaAPI.setRequestTimeoutSeconds(300);
		boolean modelLoaded = false;
        List<Model> models;
		try {
			models = ollamaAPI.listModels();
			for (Model m: models) {
				if (m.getName().startsWith("llama3.2:1b")) {
					modelLoaded = true;
				}
				logger.info(m.getName());
			}
			
		} catch (Exception e) {
			logger.error("", e);
		}

		if (!modelLoaded) {
			logger.info("loading model...");
			try {
				ollamaAPI.pullModel("llama3.2:1b");
			} catch (Exception e) {
				logger.error("", e);
			}
		}
        
	}
}
