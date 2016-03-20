ShodanScanner
=============

ShodanScanner is a multithreaded shodan search results exporter. It uses some clever tricks to bypass limitations in results and returns lots of results for specific searches even with a free account.

It does not need a payed account, but if you have one the number of the results returned are even greater.

Set shodan.ini with the appropriate options.

    [TorWorkerManager]
    #default is 50
    threads=5
    #default is empty String ""
    prefix=
    #default is true;
    torRangeStart=300
    #How ofter to save the current processed number. Default is 300
    saveEvery=50
    #useTor can be true or false
    #default is true for security, only writting false will disable tor.
    #If you want to useTor, please make sure tor executable is added in your Operating system's Path
    useTor=true
    
    [Shodan]
    #query=port:81 goahead 5ccc069c403ebaf9f0171e9517f40e41
    #query=Server: squid
    query=JAWS/1.0
    shodanUsername=shodanUsername
    shodanPassword=shodanPassword
    
Then run Main class with shodan.ini as argument to collect. 

Main.java is a complete example.
