MockServer
==========

Simple REST server that makes simulating API easy.

You can configure what response should mock server return for each path/method/queries combination (be it a json, or different file) and simulate network delay.

To use create a config file that contains on what calls server should respond in what way and start a server:

```java
HttpMockServer.startMockApiServer(configInputStream, fileReader, NetworkType.VPN);
```

Example config:

```json
{
    "port": 8098,
    "requests": [
        {
            "method": "GET",
            "path": "/books",
            "request": "",
            "code": 200,
            "response file": "books.json"
        },
        {
            "method": "POST",
            "path": {
                "base": "/authentication/login",
                "queries": {
                    "username": "touk@touk.pl",
                    "password": "abcd"
                }
            },
            "request": "",
            "code": 200,
            "response": "OK",
            "response headers": {
                "Set-Cookie": "JSESSIONID=F2D244A6B7AC4381F7B02B9AB5163829; HttpOnly"
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
            "request": "",
            "code": 401,
            "response": "Auth failed."
        },
        {
            "method": "DELETE",
            "path": {
                "urlPattern": "/selfCare/[0-9]+",
                "queries": {
                    "otp": ".*"
                }
            },
            "request": "",
            "code": 204,
            "response": ""
        }
    ]
}
```
