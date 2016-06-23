package adshotrunner.utilities;

import java.net.URI;
import java.net.URISyntaxException;

public class URLTool {

	static public String removeProtocol(String url) {
		return url.replaceAll(".*?://", "");
	}
	
	static public String setProtocol(String protocol, String url) {
		url = URLTool.removeProtocol(url);
		return protocol + "://" + url;
	}
	
	static public String getDomain(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return "";
		}
	    return uri.getHost();
	}
	
}
