Run server with CMD
===================

Customize mock server by command line arguments.
Start the server with custom resources location and selected network type.

# Preparation

### generate JAR file

Clone Project to your directory, setup project and generate JAR file
by ```sampleCompleteJar``` task.

For example run terminal in project location and write:
```
./gradlew :sample:sampleCompleteJar
```

###  prepare mock resources
Firstly,this step required to prepare own config.json .
  
Example solution:

```json
{
  "port": 8099,
  "requests": [
    {
      "method": "GET",
      "path": "/books",
      "response file": "books/books.json"
    }
  ]
}
```
Create a folder and put there config.json 
(e.g. MyMockFolder/config.json )

Then, create declared mock files if needed and put to created mock folder.
In our example we must prepare books.json and put it 
in /MyMockFolder/books/ directory.

Example solution:
 
```json
 [{
   "title": "book1",
   "properties": {
     "author": "anonymus",
     "release date": "2014"
   }
 },
   {
     "title": "book2 ",
     "properties": {
       "author": "author",
       "release date": "2000"
     }
   }
 ]
```

<b>SUMMARY</b>
We have:  sample.jar and mock resources in created mock resources folder.

In our case:
```
MyMockFolder/
        * books/
            * books.json
        * config.json
```

#  how to run?
<b>The general formula is:</b>
```
usage: java -jar sample.jar /pathToMockResources [-h] [-n <arg>]

 -h           help message
 -n <arg>     network type(GPRS,EDGE,UMTS,VPN,NO_DELAY)

```

<b>and in practise:</b>

Search your output directory for file "sample.jar" and run terminal there.

Enter in command line:
```java -jar sample.jar /path/to/mock/resources"```
then the server will start running.

You could also customize network type. Network type determines 
the network delay(The fastest network option is mode NO_DELAY,
the slowest is mode VPN).To do this,
enter in command line:
```java -jar sample.jar /path/to/mock/resources -n NO_DELAY```

Possible network types: NO_DELAY,UMTS,EDGE,GPRS,VPN.

To get help just add "-h" argument.

# Check the result

Simply clever!
Open your web browser and enter
```localhost:{port declared in config.json}/{path declared in config.json}```

in our simple case it looks like ```http://localhost:8099/books```

On Linux you can also check result in other terminal by 
curl, in our case ```curl http://localhost:8099/books```














   
 
