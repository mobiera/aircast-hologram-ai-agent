package io.twentysixty.sa.res.c;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.twentysixty.sa.client.model.message.BaseMessage;
import io.twentysixty.sa.client.model.message.IdMessage;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;


@RegisterRestClient
public interface MessageResource  {

	@POST
	  @Path("/v1/message")
	  @Produces("application/json")
	  public IdMessage sendMessage(BaseMessage message);
	
}
