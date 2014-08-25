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
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public interface ConfigReader {

    /**
     * Provide input stream which includes port and custom responses.
     */
    InputStream getMainConfigFile();

    /**
     * Provide input stream for file that contains response for single HTTP
     * call.
     *
     * @param fileName name of file that was in main config.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    InputStream getResponseConfigFromFile(String fileName) throws IOException;

    /**
     * Provide a static file that should be served for GET call.
     * If no file should be returned return null or throw {@link IOException}
     *
     * @param fileName name of file that was requested.
     * @throws java.io.IOException if there is no such a file and 404 should be
     * returned.
     */
    InputStream getStaticFile(String fileName) throws IOException;
}
