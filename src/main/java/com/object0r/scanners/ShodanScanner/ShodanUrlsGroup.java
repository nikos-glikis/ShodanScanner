package com.object0r.scanners.ShodanScanner;

import java.util.Vector;

public class ShodanUrlsGroup
{
    Vector<String> urls = new Vector<String>();

    public void addUrl(String url)
    {
        urls.add(url);
    }

    public Vector<String> getUrls()
    {
        return urls;
    }

}
