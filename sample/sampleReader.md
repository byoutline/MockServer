SampleReader implements ConfigReader example
=============
```java
public class SampleReader implements ConfigReader  {
    @Override
    public InputStream getMainConfigFile() {
    
        //return input stream with config.json
        //make sure it is not null(the paths may differ depending on how the project run) 
        
        File file = new File("sample/src/main/resources/config.json");
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    @Override
    public InputStream getPartialConfigFromFile(String relativePath) throws IOException {
    
        //in our example data are stored in the resources directory
        //if you will use own file location,you must specify a directory here
        //before relative path(defined in config.json as response file)
        
        FileInputStream fileInputStream = new FileInputStream("sample/src/main/resources/" + relativePath);
        return fileInputStream;
    }

    @Override
    public InputStream getStaticFile(String relativePath) throws IOException {
        //needed when use static data like images
        return null;
    }

    @Override
    public boolean isFolder(String relativePath) {
    
    /*
    the search file in specified path requires checking whether 
    the current location is a directory or  the target file
    */
    
        try {
            boolean directory = new File(relativePath).isFile();
            return !directory;
        } catch (Exception e) {
            return false;
        }
    }
}
```