package com.byoutline.mockserver.sample;

import com.byoutline.mockserver.ConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by michalp on 04.04.16.
 */
public class SampleReader implements ConfigReader  {
    boolean customOptions = false;
    String customConfigPath;

    public SampleReader(String path) throws Exception {
        if(path==null) throw new Exception("path can not be null");
        System.out.println("Selected path:"+path);
        customOptions=true;
        customConfigPath=path;
    }

    public SampleReader(){

    }

    @Override
    public InputStream getMainConfigFile() {

        //return input stream with config.json
        //make sure it is not null(the paths may differ depending on how the project run)
        File file;
        if(customOptions) {
            System.out.println("Config path:"+customConfigPath+"/config.json");
            file = new File(customConfigPath+"/config.json");
        }else {
            file = new File("sample/src/main/resources/config.json");
        }

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
        FileInputStream fileInputStream;
        if(customOptions){
            fileInputStream = new FileInputStream(customConfigPath+"/" + relativePath);
        }else{
           fileInputStream = new FileInputStream("sample/src/main/resources/" + relativePath);
       }

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
