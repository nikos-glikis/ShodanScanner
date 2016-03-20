package com.object0r.scanners.ShodanScanner;

import com.object0r.TorRange.ProxyWorkerManager;
 import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Ini;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;

public class ShodanWorkerManager extends ProxyWorkerManager
{
    //Max pages even with a subscription.
    public static final int MAX_PAGES = 1000;
    String cookie;
    String query;
    String shodanUsername;
    String shodanPassword;
    static Vector<String> urls = new Vector<String>();
    static Vector<String> freshUrls = new Vector<String>();
    static boolean useTorOnLogin = false;
    final String pagePlaceholder = "{{page}}";

    Vector<String> urlsGroupsToScan;

    public long getDoneCount()
    {
        return index;
    }

    public long getTotalJobsCount()
    {
        if (urlsGroupsToScan == null)
        {
            return 0;
        }
        return urlsGroupsToScan.size();
    }

    public String[] getCountriesList()
    {
        String countries[] = {"AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BQ", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CW", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SX", "SK", "SI", "SB", "SO", "ZA", "GS", "SS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW"};
        return countries;
    }

    public ShodanWorkerManager(String iniFilename)
    {
        super(iniFilename);
        for (int i = 0 ; i < this.getThreadCount(); i++)
        {
            new ShodanWorker( this, i).start();
            try { Thread.sleep(500); } catch (Exception e) {}
        }
        if (!new File("output/").exists()) {
            new File("output/").mkdirs();
        }
    }

    int index = 0;

    public synchronized String getNextEntry()
    {
        if (index >= urlsGroupsToScan.size())
        {
            try
            {
                Thread.sleep(90000);
                System.exit(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return getNextEntry();
        }
        else
        {
            return urlsGroupsToScan.get(index++);
        }
    }

    @Override
    public void prepareForExit()
    {

    }

    @Override
    public void readOptions(String filename)
    {
        try
        {
            loadCities();
            Ini prefs = new Ini(new File(filename));
            this.query = (prefs.get("Shodan", "query"));
            //this.cookie = (prefs.get("Shodan", "cookie"));
            System.out.println(prefs.get("Shodan", "query"));
            try
            {
                shodanUsername = prefs.get("Shodan", "shodanUsername");
                shodanPassword = prefs.get("Shodan", "shodanPassword");

            } catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                String loginProxy = prefs.get("Shodan", "useTorOnLogin");
                useTorOnLogin = Boolean.parseBoolean(loginProxy);
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }

            System.out.println("Use Tor On Login: "+useTorOnLogin);

            if (System.getenv("shodanUsername") != null && System.getenv("shodanPassword") != null)
            {
                shodanUsername = System.getenv("shodanUsername");
                shodanPassword = System.getenv("shodanPassword");
            }
            if (haveShodanCredentials())
            {
                System.out.println("Shodan Username from ini: " + shodanUsername);
                System.out.println("Shodan Password from ini: " + shodanPassword);
                generateCookie();
            }
            generateUrls();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void generateCookie()
    {
        try
        {
            System.out.println("Trying to generate a cookie.");
            HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_38);

            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            if (this.useTor())
            {
                System.out.println("Communication is over tor. Make sure that tor service is enabled and that tor executable is added in the system PATH.");
                //htmlUnitDriver.setSocksProxy("localhost", 9050);
            }

            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            //htmlUnitDriver.setJavascriptEnabled(true);

            //htmlUnitDriver.get("https://www.shodan.io/");
            htmlUnitDriver.get("https://account.shodan.io/login");

            Thread.sleep(2000);
            if (htmlUnitDriver.getPageSource().contains("Please complete the security check to access"))
            {
                System.out.println("There is a captcha in the page. This sometimes happens with tor. Disable TOR in shodan.ini or restart tor service.");
                System.exit(-1);
            }
            htmlUnitDriver.findElement(By.name("username")).sendKeys(shodanUsername);
            htmlUnitDriver.findElement(By.name("password")).sendKeys(shodanPassword);
            htmlUnitDriver.findElement(By.name("login_submit")).submit();
            Set<Cookie> cookies = htmlUnitDriver.manage().getCookies();
            String response = htmlUnitDriver.getPageSource();
            if (response.contains("valid username or passwo"))
            {
                System.out.println("Seems that password is incorrect. Please correct and rerun.");
                Thread.sleep(10000);
            }
            else
            {
                System.out.println("Login was successful.");
                this.cookie = "";

                for (Cookie cookie : cookies)
                {
                    if (cookie.toString().contains("; "))
                    {
                        this.cookie += cookie.getName() + "=" + cookie.getValue() + "; ";
                        //this.cookie = cookie.toString().substring(0, cookie.toString().indexOf("\"; \""));
                    }
                }
                this.cookie = this.cookie.substring(0, this.cookie.length() - 2);
                System.out.println("Cookie set to: " + this.cookie);
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Something wrong happened.");
            System.exit(-1);
        }
        //TODO remove me
        this.cookie = "__cfduid=dc5423f43e6dd5e4ed64fe309227fb8ba1456049527; _ga=GA1.2.1230545018.1456049528; _LOCALE_=en; session=\"1418bd9e6411612c3eeb3cab74fda12113f795d3gAJVQDVmYWEyOTAyNGU3ODgxNzliNDhiMzMyYzFlMWU4OGE2NzUwODM2NGI4NDBiNGNiM2VjZDVhYjRiNTFjZjM2MzdxAS4\\075\"; polito=\"0dd08171d6adba4ee67dfa48f9dae9b256ce25f856096e5ee44985157c363f22!\"; _gat=1";
    }

    private boolean haveShodanCredentials()
    {
        if (shodanPassword == null || shodanUsername == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public synchronized void addUrls(Vector<String> thisUrls)
    {
        urls.addAll(thisUrls);
        freshUrls.addAll(thisUrls);
    }

    public synchronized Vector<String> getUrls()
    {
        return this.urls;
    }

    public synchronized Vector<String> getAndCleanFresh()
    {
        Vector<String> returnVector = new Vector<String>(freshUrls);

        freshUrls = new Vector<String>();
        return returnVector;
    }

    public synchronized int getUrlsCount()
    {
        return urls.size();
    }

    class Country
    {
        public String country;
        public Vector<String> cities = new Vector<String>();

        public void addCity(String city)
        {
            cities.add(city);
        }

        public String getCountry()
        {
            return country;
        }

        public Vector<String> getCities()
        {
            return cities;
        }

        public void setCountry(String country)
        {
            this.country = country;
        }
    }

    public Vector<String> getLargeCities()
    {
        Vector<String> cities = new Vector<String>();
        try
        {
            Scanner sc = new Scanner(new FileInputStream("input/largeCities.txt"));
            while (sc.hasNext())
            {
                cities.add(sc.nextLine());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cities;
    }

    public Vector<Country> loadCities()
    {
        //CountriesToCities.json
        Vector<Country> countries = new Vector<Country>();
        try
        {
            File file = new File("input/cities.json");
            String text;

            if (file == null || !file.exists())
            {
                //InputStream is = getClass( ).getResourceAsStream("cities.json");
                InputStream is = getClass().getResourceAsStream("/cities.json");
                text = IOUtils.toString(is, "UTF-8");
            }
            else
            {
                text = FileUtils.readFileToString(file, "UTF-8");
            }


            JSONParser parser = new JSONParser();
            Object obj = parser.parse(text);
            JSONObject jsonObject = (JSONObject) obj;
            Set<Map.Entry> set = jsonObject.entrySet();

            for (Map.Entry entry : set)
            {
                Country country = new Country();
                country.setCountry((String) entry.getKey());

                org.json.simple.JSONArray c = (org.json.simple.JSONArray) entry.getValue();
                for (int i = 0; i < c.size(); i++)
                {
                    country.addCity((String) c.get(i));
                }
                countries.add(country);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return countries;
    }


    private void generateUrls()
    {
        try
        {
            String[] countries = getCountriesList();
            //Vector cities = loadCities();
            //System.out.println(cities.size());

            urlsGroupsToScan = new Vector<String>();


            //Basic Search

            urlsGroupsToScan.add("https://www.shodan.io/search?query=" + query + "&page=" + pagePlaceholder);


            for (String country : countries)
            {
                String url = "https://www.shodan.io/search?query=" + query + "+country%3A\"" + country + "\"&page=" + pagePlaceholder;
                urlsGroupsToScan.add(url);
            }

            Vector<String> largeCities = getLargeCities();

            for (String largeCity : largeCities)
            {
                String url = "https://www.shodan.io/search?query=" + query + "+city%3A\"" + largeCity + "\"&page=" + pagePlaceholder;
                urlsGroupsToScan.add(url);
            }

            Vector<Country> countriesCities = loadCities();

            Collections.shuffle(countriesCities);

            for (Country country : countriesCities)
            {
                for (String city : country.getCities())
                {
                    String url = "https://www.shodan.io/search?query=" + query + "+city%3A\"" + city + "\"&page=" + pagePlaceholder;
                    urlsGroupsToScan.add(url);
                }
            }

            System.out.println("Generated " + urlsGroupsToScan.size() + " generic urls. Each of these might have up to 1000 pages of results.");
            //Collections.shuffle(urlsGroupsToScan);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized public void writeVectorToFile(Vector<String> urls, String filename)
    {
        try
        {
            PrintWriter pr = new PrintWriter(filename);
            for (String url : urls)
            {
                pr.println(url);
            }
            pr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getCookie()
    {
        return this.cookie;
    }

    public boolean haveCookie()
    {
        if (this.cookie == null)
        {
            return false;
        }
        if (this.cookie.equals(""))
        {
            return false;
        }
        return true;
    }

    public String getQuery()
    {
        return this.query;
    }

}