MockServer Sample
=================

Simple example of using MockSerwer as a real Http serwer running 
on java virtual machine. 

## Preparation

All we need  is config.json file wchich contains configuration of own
http server and response files. Simple example shown below.

### example config.json file
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
##### tips for configuration
 * make sure that the port is not in use by another process (and if it is,
 try to change the port or kill the process that uses this port if it is 
 negligible)
 * response file field requires a relative path to resources
 * path field define how to refer to resource 
 (in this case by ```http://localhost:8099/books```)
 * If you do not specify a response file server by default tries to refer to index.html
 * by default response file should be a json file, in another case you should use
    methods for getting static data
 
### example response file
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
## Usage
##### Step 1 - Create class implements ConfigReader
[Sample Reader](sampleReader.md)

#####  Step 2 - start the serwer
```java
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) {
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);
        
        //after work
        shutDownSerwer();
    }


    private static void shutDownSerwer() {
        try {
            Thread.sleep(100000);
            httpMockServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```