package com.object0r.scanners.ShodanScanner;

import com.object0r.TorRange.TorWorker;
import com.object0r.toortools.Utilities;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShodanWorker extends TorWorker
{
    public ShodanWorkerManager manager;

    int sleepSecondsBetweenErrors = 3;
    int getErrors = 0;
    //if getErrorsLimit happen in na row, we stop processing this batch.
    int getErrorsLimit = 5;
    static int globalCount = 0;

    public ShodanWorker(ShodanWorkerManager manager, int id)
    {
        super(manager, id);
        this.manager = manager;
    }

    @Override
    public void process(String entry)
    {
        try
        {
            //String url = "https://www.shodan.io/search?query="+manager.getQuery()+"&page="+entry;
            Vector<String> thisUrls = new Vector<String>();
            for (int i=0 ; i <= manager.MAX_PAGES ;i ++)
            {
                //manager.lockEverybodyForSeconds(0);
                try
                {
                    String url = entry.replace(manager.pagePlaceholder, i+"");
                    String page = Utilities.readUrl(url, manager.getCookie());
                    Pattern p = Pattern.compile("<div class=\"search-result\">\n<div class=\"[^\"]*\"><a href=\"[^\"]*\"");
                    Matcher m = p.matcher(page);
                    if (page.contains("<p>Result limit reached.</p>")
                            ||page.contains("<div class=\"msg alert alert-info\">No results found</div>")
                            ||page.contains("Please login to use search filters")
                            ||page.contains("lease purchase a Shodan membership to access more ")
                            ||page.contains("No results found")
                            )

                    {
                        //System.out.println("Rejected");
                        setErrorsZero();
                        break;
                    }
                    while (m.find())
                    {
                        String temp = m.group();
                        String link = Utilities.cut("<a href=\"", "\"", temp);
                        //System.out.println(link);
                        thisUrls.add(link);
                    }
                    globalCount++;
                    manager.addUrls(thisUrls);
                    thisUrls = new Vector<String>();
                    if (globalCount % 30 == 0)
                    {
                        System.out.println("ShodanScanner: Collected "+manager.getUrlsCount()+" up until now.");
                    }

                    if (globalCount % 500 == 0)
                    {
                        System.out.println("Writing results to file.");
                        manager.writeVectorToFile(manager.getUrls(), "output/urls.txt");
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Some error happened: "+e);
                    increaseErrorsCount();
                    if (getErrors > getErrorsLimit)
                    {
                        System.out.println("Breaking Bad: "+entry);
                        break;
                    }
                    Thread.sleep(sleepSecondsBetweenErrors*1000);
                    continue;

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                setErrorsZero();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setErrorsZero()
    {
        getErrors=0;
    }

    private void increaseErrorsCount()
    {
        getErrors++;
    }
}
