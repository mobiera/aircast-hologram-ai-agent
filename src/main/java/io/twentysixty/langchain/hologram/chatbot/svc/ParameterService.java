package io.twentysixty.langchain.hologram.chatbot.svc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.mobiera.aircast.api.vo.ParameterVO;
import com.mobiera.aircast.commons.enums.ParameterName;
import com.mobiera.ms.mno.aircast.svc.AbstractParameterService;
import com.mobiera.ms.mno.aircast.svc.ParameterResourceInterface;

import dev.langchain4j.agent.tool.Tool;
import io.twentysixty.langchain.hologram.chatbot.res.c.PmResource;

@ApplicationScoped
public class ParameterService extends AbstractParameterService {

	@RestClient
	@Inject
	PmResource parameterResource;
	
	
	@Override
	protected ParameterResourceInterface getParameterResource() {
		return parameterResource;
	}
	
	private static HashMap<ParameterName, String> stringParams = new HashMap<ParameterName, String>();
	private static HashMap<ParameterName, Integer> intParams = new HashMap<ParameterName, Integer>();
	private static HashMap<ParameterName, Boolean> booleanParams = new HashMap<ParameterName, Boolean>();
	private static HashMap<ParameterName, Long> longParams = new HashMap<ParameterName, Long>();
	private static HashMap<ParameterName, Long> tss = new HashMap<ParameterName, Long>();
	
	private static final Logger logger = Logger.getLogger("ParameterService");
	
	
	
	public Long getAsLong(ParameterName name) {
		
		Long value = null;
		synchronized (longParams) {
			Long ts = tss.get(name);
			if ( (ts == null) || (System.currentTimeMillis()-ts>10000l)) {
				ParameterVO vo = getParameterResource().getParameterRequest(name.toString());
				String strValue = vo.getValue();
				if (strValue != null) {
					try {
						value = Long.parseLong(vo.getValue());
					} catch (Exception e) {
						logger.error("getAsLong:", e);
					}
				}
				
				longParams.put(name, value);
				tss.put(name, System.currentTimeMillis());
			} else {
				value = longParams.get(name);
			}
			
		}
		
		return value;
		
	}

	@Tool("Get an Aircast configuration parameter as a string")
	public String getAsString(ParameterName name) {
		
		String value = null;
		
		synchronized (stringParams) {
			Long ts = tss.get(name);
			if ( (ts == null) || (System.currentTimeMillis()-ts>10000l)) {
				ParameterVO vo = getParameterResource().getParameterRequest(name.toString());
				stringParams.put(name, vo.getValue());
				value = vo.getValue();
				tss.put(name, System.currentTimeMillis());
			} else {
				value = stringParams.get(name);
			}
		}
		
		
		return value;
		
	}

	public Integer getAsInteger(ParameterName name) {
		Integer value = null;
		
		synchronized (intParams) {
			Long ts = tss.get(name);
			if ( (ts == null) || (System.currentTimeMillis()-ts>10000l)) {
				ParameterVO vo = getParameterResource().getParameterRequest(name.toString());
				String strValue = vo.getValue();
				if (strValue != null) {
					try {
						value = Integer.parseInt(vo.getValue());
					} catch (Exception e) {
						logger.error("getAsInteger:", e);
					}
				}
				
				intParams.put(name, value);
				tss.put(name, System.currentTimeMillis());
			} else {
				value = intParams.get(name);
			}
			
		}
		
		return value;
	}

	public boolean getAsBoolean(ParameterName name) {
		
		Boolean value = null;
		
		synchronized (booleanParams) {
			Long ts = tss.get(name);
			if ( (ts == null) || (System.currentTimeMillis()-ts>10000l)) {
				//logger.info("getAsBoolean: not in cache: " + name);
				ParameterVO vo = getParameterResource().getParameterRequest(name.toString());
				String strValue = vo.getValue();
				if (strValue != null) {
					try {
						
						Integer intValue = Integer.parseInt(vo.getValue());
						if (intValue != null) {
							if (intValue>0) {
								value = true;
							} else {
								value = false;
							}
						}
					} catch (Exception e) {
						logger.error("getAsBoolean:", e);
					}
				}
				
				booleanParams.put(name, value);
				tss.put(name, System.currentTimeMillis());
			} else {
				//logger.info("getAsBoolean: in cache: " + name);
				value = booleanParams.get(name);
			}
			
		}
		
		
		return value;
	}
	
	
	public String STRING_NODE_TIMEZONE() {
		return getAsString(ParameterName.STRING_NODE_TIMEZONE);
		
	}
	
	
	
	
	
	

}
