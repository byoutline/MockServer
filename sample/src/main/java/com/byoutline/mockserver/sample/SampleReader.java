package com.byoutline.mockserver.sample;

import com.byoutline.mockserver.ConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

public class SampleReader implements ConfigReader {

    private boolean customOptions = false;
    private String customConfigPath;

    public SampleReader(@Nonnull String path) {
        System.out.println("Selected path:" + path);
        customOptions = true;
        customConfigPath = path;
    }

    public SampleReader() {
    }

    @Override
    public InputStream getMainConfigFile() {
        // return input stream with config.json
        // make sure it is not null(the paths may differ depending on how the project run)
        File file;
        if (customOptions) {
            file = new File(customConfigPath + "/config.json");
        } else {
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
        // in our example data are stored in the resources directory
        // if you will use own file location,you must specify a directory here
        // before relative path(defined in config.json as response file)
        FileInputStream fileInputStream;
        if (customOptions) {
            fileInputStream = new FileInputStream(customConfigPath + "/" + relativePath);
        } else {
            fileInputStream = new FileInputStream("sample/src/main/resources/" + relativePath);
        }

        return fileInputStream;
    }

    @Override
    public InputStream getStaticFile(String relativePath) throws IOException {
        FileInputStream staticInputStream;
        if (customOptions) {
            String path = customConfigPath + "/static/" + relativePath;
            staticInputStream = new FileInputStream(path);
        } else {
            staticInputStream = new FileInputStream("sample/src/main/resources/static/" + relativePath);
        }
        return staticInputStream;
    }

    @Override
    public boolean isFolder(String relativePath) {
        // the search file in specified path requires checking whether
        // the current location is a directory or  the target file
        try {
            return new File(relativePath).isDirectory();
        } catch (Exception e) {
            return false;
        }
    }
}
