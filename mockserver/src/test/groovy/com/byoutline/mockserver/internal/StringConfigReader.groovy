package com.byoutline.mockserver.internal

import com.byoutline.mockserver.ConfigReader
import org.apache.commons.io.IOUtils

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class StringConfigReader implements ConfigReader {
    String config
    Map<String, String> fileMap

    StringConfigReader(String config) {
        this(config, [:])
    }

    StringConfigReader(String config, Map<String, String> fileMap) {
        this.config = config
        this.fileMap = fileMap
    }

    @Override
    InputStream getMainConfigFile() {
        return IOUtils.toInputStream(config)
    }

    @Override
    InputStream getPartialConfigFromFile(String relativePath) throws IOException {
        def response = fileMap.get(relativePath)
        if (response == null) {
            return null
        }
        return IOUtils.toInputStream(response)
    }

    @Override
    InputStream getStaticFile(String relativePath) throws IOException {
        return null
    }

    @Override
    boolean isFolder(String relativePath) {
        return false
    }
}
