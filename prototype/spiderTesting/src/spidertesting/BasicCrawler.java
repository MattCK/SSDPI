/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spidertesting;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.ArrayList;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.Header;

/**
 *
 * @author matt
 */
public class BasicCrawler extends WebCrawler {
    
    //private String Domain;
    //private ArrayList<String> pathList = new ArrayList<String>();
    private ArrayList<crawledInfo> crawlingData = new ArrayList<crawledInfo>();
    private linkKeeper LinkKeeper = new linkKeeper();
    private static String mainDomainName;
    //super.
    /*public void WebCrawler()
    {
        crawledInfo initializer = new crawledInfo();
        initializer.path = "";
        initializer.pathCount = 0;
        initializer.totalLinksInPath = 0;
        crawlingData.add(initializer);
    }*/
    
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
                    + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public static void configure(String DomainName) {

        BasicCrawler.mainDomainName = DomainName;
        
	}
    
    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(WebURL url) {
            String href = url.getURL().toLowerCase();
            //System.out.println("before returning should visit - " + !FILTERS.matcher(href).matches() + " - " + href.contains(url.getDomain()));
            return !FILTERS.matcher(href).matches() && href.contains(mainDomainName);
            //return true;
    }
    
    	// This function is called by controller to get the local data of this
	// crawler when job is finished
	@Override
	public Object getMyLocalData() 
        {
		//return crawlingData;
                return LinkKeeper;
	}

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
            int docid = page.getWebURL().getDocid();
            String url = page.getWebURL().getURL();
            String domain = page.getWebURL().getDomain();
            String path = page.getWebURL().getPath();
            String subDomain = page.getWebURL().getSubDomain();
            String parentUrl = page.getWebURL().getParentUrl();
            String anchor = page.getWebURL().getAnchor();

            //System.out.println("Docid: " + docid);
            //System.out.println("URL: " + url);
            //System.out.println("Domain: '" + domain + "'");
            //System.out.println("Sub-domain: '" + subDomain + "'");
            //System.out.println("Path: '" + path + "'");
            //System.out.println("Parent page: " + parentUrl);
            //System.out.println("Anchor text: " + anchor);
            
            //System.out.println("why am I not getting the parsedoc output?");
            List<WebURL> links = new ArrayList<WebURL>();
            if (page.getParseData() instanceof HtmlParseData) {
                    HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                    String text = htmlParseData.getText();
                    String html = htmlParseData.getHtml();
                    links = htmlParseData.getOutgoingUrls();
                    /*
                    for(WebURL currURL: links)
                    {
                        System.out.println(currURL.getURL());
                    }
                    */
                    System.out.println("Text length: " + text.length());
                    System.out.println("Html length: " + html.length());
                    System.out.println("Number of outgoing links: " + links.size());
            }
            LinkKeeper.addListOfLinks(links, mainDomainName);

            Header[] responseHeaders = page.getFetchResponseHeaders();
            
            /*if (responseHeaders != null) {
                    System.out.println("Response headers:");
                    for (Header header : responseHeaders) {
                            System.out.println("\t" + header.getName() + ": " + header.getValue());
                    }
            }*/
                    
            boolean pathAlreadyExists = false;
            int matchAt = 0;
            //System.out.println("About to go into crawling data loop");
            for (int i = 0; i < crawlingData.size();i++) 
            {    
                
                //firstTwoSlashes = 
                //System.out.println("Stuck in the drive through - " + Integer.toString(i));
                crawledInfo currentCrawl = (crawledInfo) crawlingData.get(i);
                if (currentCrawl.path.equals(path))
                {
                    pathAlreadyExists = true;
                    matchAt = i;
                }
                    
            }
            
            if(pathAlreadyExists)
            {
                //System.out.println("modifying crawled info");
                crawledInfo changeCrawlInfo = (crawledInfo) crawlingData.get(matchAt);
                changeCrawlInfo.pathCount++;
                crawlingData.set(matchAt, changeCrawlInfo);
            }
            else
            {
                //System.out.println("adding to crawled info");
                crawledInfo adToCrawlInfo = new crawledInfo();
                //System.out.println("after creating a crawledInfo");
                adToCrawlInfo.path = path;
                //System.out.println("after setting a property in crawledInfo");
                adToCrawlInfo.pathCount = 1;
                adToCrawlInfo.anchor = anchor;
                adToCrawlInfo.parentPage = parentUrl;
                adToCrawlInfo.URL = url;
                
                //adToCrawlInfo.title = 
                //adToCrawlInfo.totalLinksInPath = 
               //System.out.println("before touching crawlingData");
                crawlingData.add(adToCrawlInfo);
                //System.out.println("it clearly doesn't like something there.");
            }

            System.out.println("=============");
	}   
    
}
