package com.byoutline.mockserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps platform specific bits.
 * For android implementation will look something like this:
 * <code class="java">
 * 
 * </code>
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public interface ConfigReader {

    /**
     * Provide input stream for file that contains response for single REST
     * method.
     */
    InputStream getResponseConfigFromFileAsStream(String fileName) throws IOException;

    /**
     * Provide list of file names from folder where file responses(fe: images)
     * are located.
     */
    String[] getResponseFolderFileNames();

    /**
     * Provide a file with given name from folder with file responses(it
     * probably won't be text file).
     */
    public File getResponseFile(String fileName);
}
