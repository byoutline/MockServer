package com.byoutline.mockserver.internal

import com.byoutline.mockserver.DefaultValues
import spock.lang.Specification


/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class ConfigParserSpec extends Specification {
    def "should return empty config if config file is empty"() {
        given:
        def reader = new StringConfigReader("")
        when:
        def json = ConfigParser.getMainConfigJson(reader)
        def instance = new ConfigParser(reader)
        def result = instance.parseConfig(json)
        then:
        result.responses.isEmpty()
        result.port == DefaultValues.MOCK_SERVER_PORT
    }

    def "should set port from config"() {
        given:
        def reader = new StringConfigReader('''{"port": 1234}''')
        when:
        def json = ConfigParser.getMainConfigJson(reader)
        def instance = new ConfigParser(reader)
        def result = instance.parseConfig(json)
        then:
        result.port == 1234
    }
}