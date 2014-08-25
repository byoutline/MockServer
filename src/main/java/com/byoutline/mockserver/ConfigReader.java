package com.byoutline.mockserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps platform specific bits.
 *
 * @see <a
 * href="https://github.com/byoutline/AndroidMockServer">AndroidMockServer
 * implementation</a>
 *
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public interface ConfigReader {

    /**
     * Provide input stream which includes port and custom responses.
     */
    InputStream getMainConfigFile();

    /**
     * Provide input stream for file that contains response for single REST
     * method.
     *
     * @param relativeFilePath path of file that was in main config.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    InputStream getResponseConfigFromFile(String relativeFilePath) throws IOException;

    /**
     * Provide a file with given name from folder with file responses(it
     * probably won't be text file).
     *
     * @param relativeFilePath path of file that was in main config.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    public File getResponseFile(String relativeFilePath) throws IOException;
}
