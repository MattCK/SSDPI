/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spidertesting;

import edu.uci.ics.crawler4j.url.WebURL;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 *
 * @author matt
 */
public class linkKeeper {
    
    ArrayList<LinksAndCount> allLinks = new ArrayList<LinksAndCount>();
    
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
                    + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    
    public void addListOfLinks(List<WebURL> links, String domain)
    {
        for(WebURL currLink: links)
        {
        //WebURL currLink = links.get(0);
            System.out.println("Adding Link for - " + currLink.getURL() + "-  domain - " + domain);
            if(currLink.getDomain().equalsIgnoreCase(domain) && !FILTERS.matcher(currLink.getURL().toLowerCase()).matches())
            {
                boolean linkMatched = false;
                System.out.println("Testing link to add to the list");
                for(int allLinksCounter = 0; allLinksCounter < allLinks.size() && !linkMatched; allLinksCounter++)
                {
                   //if the current url == the url of where we are in the list
                   //System.out.println("Inside the allLinksList");
                   if (currLink.getURL().equalsIgnoreCase(allLinks.get(allLinksCounter).url.getURL()))
                   {
                       linkMatched = true;
                       LinksAndCount adder = allLinks.get(allLinksCounter);
                       adder.count++;
                       allLinks.add(allLinksCounter, adder);
                       System.out.println("matched URL - " + currLink.getURL() + " - count - " + Integer.toString(adder.count));
                   }
                }
                if(!linkMatched)
                {
                    LinksAndCount newLink = new LinksAndCount();
                    newLink.url = currLink;
                    newLink.count = 1;
                    allLinks.add(newLink);
                    System.out.println("Adding a new URL to the list" + currLink.getURL());
                }
            }
        }
        
        
    }
    
    
}
