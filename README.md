MockServer
==========
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.byoutline.mockserver/mockserver/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.byoutline.mockserver/mockserver)
 master:  [![Build Status](https://travis-ci.org/byoutline/MockServer.svg?branch=master)](https://travis-ci.org/byoutline/MockServer)
 develop: [![Build Status](https://travis-ci.org/byoutline/MockServer.svg?branch=develop)](https://travis-ci.org/byoutline/MockServer)
 

Simple Http server that makes simulating API easy.

You can configure what response should mock server return for each path/method/queries combination (be it a json, or different file) and simulate network delay.

To use create a config file that contains on what calls server should respond in what way and start a server:

```java
HttpMockServer.startMockApiServer(configReader, NetworkType.VPN);
```

## Config syntax
Mock server is synchronized with single json file for simplicity. At top level you can specify:

```
{
    "port" : 8000, // port at which server will be started
    "requests" :
    [
        //array of objects representing requests with responses
    ]
}
```

Request are matched top to bottom until first match is found. This allow to make specific/general approach and test e.g. success/error responses.

Each of request objects represents both request and response. Allowed fields:

### Request part

#### *method* (string) required! 
HTTP method e.g. GET/PUT/POST/DELETE

```
{
    "method":"GET"
}
```

#### *path* (string/object) required! 
Everything in url that stands after http://localhost:port e.g. /api/auth

String version specifies only base path. Object version allows also to specify query params i.e. everything that goes after "?".

Syntax for path object:

```
{
    "base": "/authentication/login", //path that goes before "?"
    "queries": //will be converted to ?username=user&password=pass1234 
    {
        "username": "user",
        "password": "pass1234"
    }
}
```

#### *body* (string)
Optional body of request, can be used to match POST/PUT calls by exact string match

### Response part
#### *code* (int)
Html response code to be returned

#### *response* (string/object)
Response body to be returned. 

#### *response file* (string) 
Alternative to *response*, name of file that contains body to be returned as response. Should be stored in directory specified by *getResponseConfigFromFile()*.

#### *response headers* (string)
Allows to specify headers that should be returned in response.

```
{
    "response headers":
    {
        "Set-Cookie": "JSESSIONID=ABCDE1234567890ABCDE1234567890; HttpOnly"
    }
}
```

Example config:

```json
{
    "port": 8098,
    "requests": [
        {
            "method": "GET",
            "path": "/books",
            "response file": "books.json"
        },
        {
            "method": "POST",
            "path": {
                "base": "/authentication/login",
                "queries": {
                    "username": "user",
                    "password": "pass1234"
                }
            },
            "code": 200,
            "response": "OK",
            "response headers": {
                "Set-Cookie": "JSESSIONID=ABCDE1234567890ABCDE1234567890; HttpOnly"
            }
        },
        {
            "method": "POST",
            "path": {
                "base": "/authentication/login",
                "queries": {
                    "username": ".*",
                    "password": ".*"
                }
            },
            "code": 401,
            "response": "Auth failed."
        },
        {
            "method": "DELETE",
            "path": {
                "urlPattern": "/books/[0-9]+",
                "queries": {
                    "otp": ".*"
                }
            },
            "code": 204,
            "response": ""
        }
    ]
}
```

For typical use Android take a look at <a href="https://github.com/byoutline/AndroidStubServer">AndroidStubServer</a>.

#### Latest Changes ####
  * 1.4.0 If folder is requested and it contains index.html it will be returned instead of 404. 
  ConfigReader requires now implementing isFolder method.
