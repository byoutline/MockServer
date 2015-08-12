package com.byoutline.mockserver;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps platform specific bits.
 *
 * @see <a
 * href="https://github.com/byoutline/AndroidStubServer">AndroidMockServer
 * implementation</a>
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public interface ConfigReader {

    /**
     * Provide input stream which includes port and custom responses.
     * If null is returned default port and no responses will be used.
     * @return stream with json string or null
     */
    InputStream getMainConfigFile();

    /**
     * Provide input stream for file that contains response for single HTTP
     * call.
     *
     * @param relativePath relative path (usually only file name) of file that was in main config.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    InputStream getResponseConfigFromFile(String relativePath) throws IOException;

    /**
     * Provide a static file that should be served for GET call.
     * If no file should be returned return null or throw {@link IOException}
     *
     * @param relativePath relative path (usually only file name) of file that was requested.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    InputStream getStaticFile(String relativePath) throws IOException;

    /**
     * Informs if given path points to a folder.
     * @param relativePath relative path to check
     * @return true if there is folder under pointed path, false otherwise.
     */
    boolean isFolder(String relativePath);
}
