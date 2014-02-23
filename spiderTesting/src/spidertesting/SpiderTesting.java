/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spidertesting;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import java.util.UUID;

/**
 *
 * @author matt
 */
public class SpiderTesting {

    /**
     * @param args the command line arguments
     */
   public static void main(String[] args) throws Exception {
       /* 
       if (args.length != 2) {
                System.out.println("Needed parameters: ");
                System.out.println("\t rootFolder (it will contain intermediate crawl data)");
                System.out.println("\t numberOfCralwers (number of concurrent threads)");
                return;
        }
       */

        /*
         * crawlStorageFolder is a folder where intermediate crawl data is
         * stored.
         */
        //String crawlStorageFolder = args[0];
        String crawlStorageFolder = "V:\\Work\\Screenshot\\crawler\\crawler4j\\scanned";
        /*
         * numberOfCrawlers shows the number of concurrent threads that should
         * be initiated for crawling.
         */
        //int numberOfCrawlers = Integer.parseInt(args[1]);
        int numberOfCrawlers = 6;

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(500);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        config.setMaxDepthOfCrawling(1);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(1000);
        

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         * 
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        WebURL site1 = new WebURL();
        //site1.setURL("http://www.abc15.com/");
        site1.setURL("http://www.glendalenewspress.com/");
        //site1.setURL("http://www.baltimoresun.com/");
        //controller.addSeed("http://www.tampabay.com/");
        controller.addSeed(site1.getURL());
        BasicCrawler.configure(site1.getDomain());
        //controller.setCustomData(controller);
        //controller.addSeed("http://www.ics.uci.edu/~welling/");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        //System.out.println("before starting the controller");
        controller.start(BasicCrawler.class, numberOfCrawlers);
        
        List<Object> crawlersPathList = controller.getCrawlersLocalData();
        
        
        StringBuilder CSVOut = new StringBuilder();
        //for (Object LinkKeeper : crawlersPathList) 
        //{
        Object LinkKeeper = crawlersPathList.get(0);
            //List<Object> currentCrawlerData = (List<Object>) currentList;
            linkKeeper currentLinkKeeper = (linkKeeper) LinkKeeper;
            for (int i = 0; i < currentLinkKeeper.allLinks.size();i++) 
            {                
                //crawledInfo currentInfo = (crawledInfo) currentCrawlerData.get(i);
                
                //if((StringUtils.countMatches(currentInfo.path, "/") <= 3) && (currentInfo.path.length() <=30))
                //{
                    //System.out.println(Integer.toString(currentInfo.pathCount) + " - " + currentInfo.path);
                    /*CSVOut.append(currentInfo.path);
                    CSVOut.append(",");
                    CSVOut.append(currentInfo.pathCount);
                    CSVOut.append(",");
                    CSVOut.append(currentInfo.URL);
                    CSVOut.append(",");
                    CSVOut.append(currentInfo.anchor);
                    CSVOut.append(",");
                    CSVOut.append(currentInfo.parentPage);
                    CSVOut.append(System.getProperty("line.separator"));
                    */
                    CSVOut.append(Integer.toString(currentLinkKeeper.allLinks.get(i).count));
                    CSVOut.append(",");
                    CSVOut.append(currentLinkKeeper.allLinks.get(i).url.getURL());
                    CSVOut.append(System.getProperty("line.separator"));
                    
                //}
                    
            } 
        //}
        
        UUID rando = UUID.randomUUID();
        FileUtils.writeStringToFile(new File("V:\\Work\\Screenshot\\crawler\\crawler4j\\csvOut\\" + site1.getDomain() + rando.toString() + ".csv"), CSVOut.toString());

        
}
    
}
