package adshotrunner.utilities;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.net.InternetDomainName;

/**
 * The URLTool parses and modifies URLs such as changing the protocol 
 * or returning the domain.
 */
public class URLTool {

	/**
	 * Removes any protocol from the passed URL (i.e. http, https, ftp)
	 * 
	 * @param url			URL to remove protocol from
	 * @return				URL with protocol removed
	 */
	static public String removeProtocol(String url) {
		return url.replaceAll(".*?://", "");
	}
	
	/**
	 * Sets the protocol of the passed URL. If the URL contains no protocol,
	 * it is added to the beginning of the string. If the URL already has a
	 * protocol, it is replaced.
	 * 
	 * @param url			URL to set protocol to
	 * @return				URL with protocol set
	 */
	static public String setProtocol(String protocol, String url) {
		url = URLTool.removeProtocol(url);
		return protocol + "://" + url;
	}
	
	
	/**
	 * Returns the full subdomain of the passed URL. (Subdomain part + domain part)
	 * The protocol is omitted (such as http:// or https://)
	 * 
	 * Example: news.artnet.com, pics.nytimes.com, gear.slashdot.org, etc.
	 * 
	 * @param url			Target URL
	 * @return				Full subdomain of the passed URL
	 */	
	static public String getSubdomain(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return "";
		}
	    return uri.getHost();
	}
	
	/**
	 * Returns the domain of the passed URL. 
	 * 
	 * The domain does not include any subdomain or any protocol (i.e. http://)
	 * 
	 * NOTE: This function works. It was, however, copy and pasted from the web by Matt
	 * 		 I've cleaned it up, but it seems to have finicky logic. For example,
	 * 		 it requires the url to have a protocol. I'm not going to take the time to 
	 * 		 improve it now, but if you're here because it's not doing what you need to,
	 * 		 take a good look at it.
	 * 
	 * @param url			Target URL
	 * @return				Domain of passed URL or empty string on failure
	 */
	static public String getDomain(String url) {
		
		//Replace all quotes in the URL. Not sure why we needed to do this, but it seems we need to.
		url = url.replace("\"", "");
		
		//If the URL has a protocol that is http/https/spdy and does not begin with javascript,
		//try parsing the URL.
		if(url.contains("://") && ((url.contains("http") || url.contains("spdy"))) && (!url.startsWith("javascript"))){

			//Try turning the passed URL string into an instance of a URL object.
			//Turn that URL object into a URI object. Use the URI object
			//and the InternetDomainName class to get the final domain and return it.
			try{
				URL urlObject = new URL(url);
				String nullFragment = null;
				URI uriObject = new URI(urlObject.getProtocol(), urlObject.getHost(), urlObject.getPath(), urlObject.getQuery(), nullFragment);
				if (uriObject.getHost() == null) {return "";}
				return InternetDomainName.from(uriObject.getHost()).topPrivateDomain().toString();
			}
			
			//If an error occurred, such as by a malformed URL, simply return an empty string.
			catch(Exception e){return "";}
		}
		
		//Otherwise return an empty string
		else {return "";}
	}
}
