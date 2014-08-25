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

Complex example of starting MockServer on Android, where we want to have: 
* config in res/raw
* responses in assets
* files for download in external storage

(Note that it is example, not suggested way of placing config files. Keeping all of configuration together have probably more sense):

```java
InputStream configInputStream = getResources().openRawResource(R.raw.config);
ConfigReader fileReader = new ConfigReader() {

    @Override
    public InputStream getResponseConfigFromFileAsStream(String fileName) throws IOException {
        return getAssets().open(fileName);
    }

    @Override
    public String[] getResponseFolderFileNames() {
        return Environment.getExternalStorageDirectory().list();
    }

    @Override
    public File getResponseFile(String fileName) {
        return new File(Environment.getExternalStorageDirectory(), fileName);
    }
};
HttpMockServer.startMockApiServer(configInputStream, fileReader, NetworkType.VPN);
```

