package adshotrunner.tests;

import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public class ResponseHeaderTest {

	public static void main(String[] args) throws Exception {
		WebClient webClient = new WebClient();
	    int code = webClient.getPage(
	            "https://s3.amazonaws.com/asr-images/fillers/nsfiller-1x1.jpg"
	    ).getWebResponse().getStatusCode();
	    webClient.closeAllWindows();
	    System.out.println("Code: " + code);
	}
}
