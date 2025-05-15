package io.twentysixty.langchain.hologram.chatbot.svc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.mobiera.aircast.api.v1.campaign.ListCampaignsRequest;
import com.mobiera.aircast.api.v1.campaign.ListCampaignsRequestAnswer;
import com.mobiera.aircast.api.v1.campaign.TestCampaignRequest;
import com.mobiera.aircast.api.v1.campaign.TestCampaignRequestAnswer;
import com.mobiera.aircast.api.v1.campaign_schedule.ListCampaignSchedulesRequest;
import com.mobiera.aircast.api.v1.campaign_schedule.ListCampaignSchedulesRequestAnswer;
import com.mobiera.aircast.api.vo.CampaignScheduleVO;
import com.mobiera.aircast.api.vo.CampaignVO;
import com.mobiera.aircast.api.vo.ParameterVO;
import com.mobiera.aircast.commons.enums.ParameterName;
import com.mobiera.commons.enums.EntityState;
import com.mobiera.ms.mno.aircast.api.CmSchedule;
import com.mobiera.ms.mno.aircast.svc.AbstractParameterService;
import com.mobiera.ms.mno.aircast.svc.ParameterResourceInterface;

import dev.langchain4j.agent.tool.Tool;
import io.twentysixty.langchain.hologram.chatbot.res.c.PmResource;

@ApplicationScoped
public class CampaignService extends AbstractParameterService {

	@RestClient
	@Inject
	PmResource pmResource;
	
	@Inject ParameterService parameterService;
	
	private static final Logger logger = Logger.getLogger(CampaignService.class);
	
	
	@Tool("List Aircast Campaigns. Set request.type=ADVERTISING for advertising campaigns, or request.type=SMS for sms campaigns.")
	public List<CampaignVO> listCampaigns(ListCampaignsRequest request) {
		
		ListCampaignsRequestAnswer result = pmResource.listCampaignsRequest(request);
		if (result != null) {
			return result.getCampaigns();
		}
		return new ArrayList<CampaignVO>();
		
	}
	
	
	
	
	@Tool("List currently executing campaigns by returning a list of CampaignScheduleVO that represent Campaigns and their schedule (when to run). Each returned CampaignScheduleVO includes at least the corresponding campaignId, campaignType, campaignName, and statistics from the current running campaigns. Statistics from today are attributes that starts with today.")
	public List<CampaignScheduleVO> listExecutingCampaigns() {
		
		String tzStr = parameterService.getAsString(ParameterName.STRING_NODE_TIMEZONE);
		
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tzStr));
		
		
		
		ListCampaignSchedulesRequest request = new ListCampaignSchedulesRequest();
		request.setFromTs(now.truncatedTo(ChronoUnit.DAYS).toInstant());
		request.setToTs(request.getFromTs().plusSeconds(86400));
		request.setIncludeStates(List.of(EntityState.ENABLED));
		ListCampaignSchedulesRequestAnswer result = pmResource.listCampaignSchedulesRequest(request);
		 
		 List<CampaignScheduleVO> campaigns = result.getCampaignSchedules();
		 
		logger.info("listExecutingCampaigns: found " + campaigns.size());
		return campaigns;
		
	}
	
	@Tool("Get an Aircast Campaign with its campaignId")
	public CampaignVO getCampaign(Long campaignId) {
		
		CampaignVO c = pmResource.getCampaign(campaignId);
		
		return c;
		
	}
	
	
	@Tool("Test an Aircast Campaign. Requires a msisdn (phone number with its country prefix), and campaignId, the id of the campaign. Campaign musn't be in DISABLED or ARCHIVED state. You can use the getCampaign tool to verify campaign state before testing the campaign.")
	public void testCampaign(String msisdn, Long campaignId) {
		TestCampaignRequest request = new TestCampaignRequest();
		request.setId(campaignId);
		request.setMsisdn(msisdn);
		request.setRequestId(0l);
		pmResource.testCampaignRequest(request);
		
		return;
		
	}
	
	

}
