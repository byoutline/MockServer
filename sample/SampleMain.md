Example of using HttpMockServer with GET Http method
====================================================
```java
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) {
        
        //start server
        
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);
        
        //do something with mock data
        
        doSomethingWithUsingHttpMockServer();
        
        // shut down server 
        shutDownSerwer();
    }

    private static void doSomethingWithUsingHttpMockServer() {
        String urlString = "http://localhost:8099/books";
        StringBuilder result = new StringBuilder();
        URL url = null;
        try {
            url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            System.out.print(result.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void shutDownSerwer() {
        try {
            httpMockServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

##### comments
* shutdown() method close server connection and stops ResponseHandler 
immediately after finish working with server. Without using this method 
system do the same after a while.
