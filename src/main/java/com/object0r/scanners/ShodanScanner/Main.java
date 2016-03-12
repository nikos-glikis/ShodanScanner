package com.object0r.scanners.ShodanScanner;


import java.util.Vector;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.out.println("No session ini in arguments.");
            System.out.println("Usage: ");
            System.out.println("java -cp java -cp out/production/TorRange-ripper/:lib/* com.circles.rippers.TorRange.Main example.ini");

            System.exit(0);
        }

        try
        {
            ShodanWorkerManager shodanWorkerManager = new ShodanWorkerManager(args[0]);

            while (true)
            {
                try
                {
                    //Each time this is called, results are returned and cleaned.
                    Vector<String> results = shodanWorkerManager.getAndCleanFresh();
                    for (String ip : results)
                    {
                        System.out.println(ip);
                    }
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
