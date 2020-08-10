MockServer Sample
=================

Simple example of using MockSerwer as a real Http serwer running locally
on java virtual machine. 

## Preparation

All we need  is:
 * config.json file that contains configuration of own http server
 * response files - json,xml or another text files
 * static resources - images, sounds etc.

Simple example shown below.

### Example config.json file
```json
{
  "port": 8099,
  "requests": [
    {
      "method": "POST",
      "path": "/books",
      "response file": "books/books.json"
    },
    {
      "method": "GET",
      "path": "/testxml",
      "response file": "simple.xml"
    },
    {
      "method": "GET",
      "path": "/something",
      "response file": "samplefile"
    }
  ]
}
```
##### configuration tips
 * make sure that the port is not in use by another process (and if it is,
 try to change the port or kill the process that uses this port if it is 
 negligible)
 * response file field requires a relative path to resource
 * path field define how to refer to resource 
 (in this case by ```http://localhost:8099/books```)
 * If you do not specify a response file server by default tries to refer to index.html
 * preferred response should be a json or xml file (json is validated, if not valid server will crash)
 * another response files will be send as text
 * static resources should be placed in directory named "static"
### Example response files
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
