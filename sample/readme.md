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
##### configuration tips
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
[Sample.class using HttpMockServer](SampleMain.md)

## Check mock data by web browser
All we need is to keep server running some time after start because 
system will shut down server after ending main code.

[Example keep server running some time](SampleWithWebBrowser.md)

Enter ```http://localhost:8099/books``` after running above example code.
If all works you should see json response file in web browser.
