package adshotrunner.archive;

import java.util.Map;

import com.google.gson.Gson;

public class CampaignResult {
	public String jobID;
	public Boolean queued;
	public Boolean success;
	public String message;
	
	public String customer;
	public String domain;
	public String date;

	public Map<String, String> screenshots;

	public String powerPointURL;
	public String zipURL;
	
	public long runtime;
		
	public CampaignResult() {}
	
	public String toJSON() {
		return new Gson().toJson(this);  
	}

}
