package io.twentysixty.langchain.hologram.chatbot.res.c;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.net.HttpURLConnection;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobiera.aircast.api.util.RoleUtil;
import com.mobiera.aircast.api.v1.campaign.ListCampaignsRequest;
import com.mobiera.aircast.api.v1.campaign.ListCampaignsRequestAnswer;
import com.mobiera.aircast.api.v1.campaign.TestCampaignRequest;
import com.mobiera.aircast.api.v1.campaign.TestCampaignRequestAnswer;
import com.mobiera.aircast.api.v1.campaign_schedule.GetCampaignScheduleRequest;
import com.mobiera.aircast.api.v1.campaign_schedule.ListCampaignSchedulesRequest;
import com.mobiera.aircast.api.v1.campaign_schedule.ListCampaignSchedulesRequestAnswer;
import com.mobiera.aircast.api.v1.generic_entity.ToggleEntityStateRequest;
import com.mobiera.aircast.api.v1.generic_entity.ToggleEntityStateRequestAnswer;
import com.mobiera.aircast.api.v1.generic_entity.ToggleEntityStateRequestResult;
import com.mobiera.aircast.api.v1.smpp_account.ListSmppAccountsRequest;
import com.mobiera.aircast.api.v1.smpp_account.ListSmppAccountsRequestAnswer;
import com.mobiera.aircast.api.vo.CampaignScheduleVO;
import com.mobiera.aircast.api.vo.CampaignVO;
import com.mobiera.aircast.api.vo.ParameterVO;
import com.mobiera.aircast.api.vo.ServiceStatusVO;
import com.mobiera.aircast.api.vo.SmppAccountVO;
import com.mobiera.aircast.commons.enums.ParameterName;
import com.mobiera.commons.enums.EntityState;
import com.mobiera.commons.exception.DataIntegrityException;
import com.mobiera.commons.exception.InvalidEntityException;
import com.mobiera.commons.exception.PermissionDeniedException;
import com.mobiera.commons.exception.ServiceNotAvailableException;
import com.mobiera.commons.util.JsonUtil;
import com.mobiera.ms.mno.aircast.api.CmSchedule;
import com.mobiera.ms.mno.aircast.svc.ParameterResourceInterface;



@RegisterRestClient
public interface PmResource extends ParameterResourceInterface {


	@GET
	@Path("/parameter/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public ParameterVO getParameterRequest(@PathParam(value = "name") String name) ;
	
	
	/*
	 * Campaign
	 * 
	 */
	
	@POST
	   @Path("/aircast-slave-api-rest-server-war/api/pub/v1/campaign/ListCampaignsRequest")
	   @Produces("application/json")
	   public ListCampaignsRequestAnswer listCampaignsRequest(ListCampaignsRequest request);
	
	@GET
	@Path("/campaign/{id}")
	   @Produces("application/json")
	   public CampaignVO getCampaign(@PathParam(value = "id") Long campaignId);
	
	
	
	/*
	 * Schedule
	 */
	
	@GET
	@Path("/schedules/enabled")
	@Produces("application/json")
	public List<CmSchedule> getEnabledSchedules();
	
	   
	@POST
	   @Path("/aircast-slave-api-rest-server-war/api/pub/v1/campaign/ListCampaignSchedulesRequest")
	   @Produces("application/json")
	   public ListCampaignSchedulesRequestAnswer listCampaignSchedulesRequest(ListCampaignSchedulesRequest request);
	
	
	@POST
	   @Path("/aircast-slave-api-rest-server-war/api/pub/v1/campaign/TestCampaignRequest")
	   @Produces("application/json")
	   public TestCampaignRequestAnswer testCampaignRequest(TestCampaignRequest request);
	
	
	@POST
	@Path("/aircast-slave-api-rest-server-war/api/pub/v1/generic_entity/ToggleEntityStateRequest")
	@Produces("application/json")
	public ToggleEntityStateRequestAnswer toggleEntityStateRequest(ToggleEntityStateRequest request);

	@POST
	   @Path("/aircast-slave-api-rest-server-war/api/pub/v1/smpp_account/ListSmppAccountsRequest")
	   @Produces(MediaType.APPLICATION_JSON)
		public ListSmppAccountsRequestAnswer listSmppAccountsRequest(ListSmppAccountsRequest request);
}
