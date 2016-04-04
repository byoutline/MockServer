```java
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) {
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);

        //keep server running some time and shut down
        //during that time we can check data in browser
        
        shutDownSerwerAfterMinute();
    }


    private static void shutDownSerwerAfterMinute() {
        try {
            Thread.sleep(60000);
            httpMockServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```