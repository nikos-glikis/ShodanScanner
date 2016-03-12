package com.object0r.scanners.ShodanScanner;


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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
