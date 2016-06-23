package adshotrunner.techpreview;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class CampaignRequest {
	public String jobID;
	public List<String> tagImages;
	public List<Map<String,String>> pages;
	
	public CampaignRequest() {}
	
	public static CampaignRequest fromJSON(String jsonMessage) {
		CampaignRequest parsedRequest = new CampaignRequest();
		parsedRequest = new Gson().fromJson(jsonMessage, CampaignRequest.class);  
		return parsedRequest;
	}
}
