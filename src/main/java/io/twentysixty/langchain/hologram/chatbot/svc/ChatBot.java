package io.twentysixty.langchain.hologram.chatbot.svc;

import java.util.UUID;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(tools = { ParameterService.class, CampaignService.class })
@ApplicationScoped
public interface ChatBot {

    String chat(@MemoryId UUID memoryId, @UserMessage String question);
}