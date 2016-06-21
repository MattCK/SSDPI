package adshotrunner.techpreview;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class ScreenshotRequest {
	public String jobID;
	public List<String> tagImages;
	public List<Map<String,String>> pages;
	
	public ScreenshotRequest() {}
	
	public static ScreenshotRequest fromJSON(String jsonMessage) {
		ScreenshotRequest parsedRequest = new ScreenshotRequest();
		parsedRequest = new Gson().fromJson(jsonMessage, ScreenshotRequest.class);  
		return parsedRequest;
	}
}
