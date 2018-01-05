package com.byoutline.mockserver.internal

import com.byoutline.mockserver.DefaultValues
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class ConfigParserRequestParsingSpec extends Specification {
    @Shared
    def path = '/a'
    @Shared
    def method = 'GET'
    @Shared
    def queryKey = 'username'
    @Shared
    def queryVal = 'user'
    @Shared
    def queryKey2 = 'a'
    @Shared
    def queryVal2 = 'b'
    @Shared
    def headerKey = 'sessionId'
    @Shared
    def headerKey2 = 'custom header'
    @Shared
    def headerVal = '1234'
    @Shared
    def headerVal2 = '5678'
    @Shared
    def bodyContent = 'body content'

    @Shared
    def requestFilePath = '/request.json'
    @Shared
    def requestFileContent = """{
        "method": "$method",
        "path": "$path"
    }"""

    @Shared
    def headerFilePath = '/header.json'
    @Shared
    def headerFileContent = """{ "$headerKey": "$headerVal"  }"""

    @Shared
    def queriesFilePath = "/queries.json"
    @Shared
    def queriesFileContent = """{"$queryKey":"$queryVal"}"""

    @Shared
    def simplePath = """{ "requests": [{
        "method": "$method",
        "path": "$path"
    } ]}"""
    @Shared
    def basePath = """{"requests": [{
        "method": "$method",
        "path": { "base": "$path" }
    } ]}"""
    @Shared
    def patternPath = """{"requests": [{
        "method": "$method",
        "path": { "url pattern": "$path" }
    } ]}"""
    @Shared
    def defaultQuery = """{"requests": [{
        "method": "$method",
        "path": {
            "base": "$path",
            "queries": { "$queryKey": "$queryVal" }
        },
    } ]}"""
    @Shared
    def exactQuery = """{"requests": [{
        "method": "$method",
        "path": {
            "base": "$path",
            "queries matching method": "EXACT",
            "queries": { "$queryKey": "$queryVal" }
        },
    } ]}"""
    @Shared
    def queryFile = """{ "requests": [{
        "method": "$method",
        "path": {
            "base": "$path",
            "queries file": "$queriesFilePath"
        }
    } ]}"""
    @Shared
    def queryFileOverride = """{ "requests": [{
        "method": "$method",
        "path": {
            "base": "$path",
            "queries file": "$queriesFilePath",
            "queries": { "$queryKey": "$queryVal2" }
        }
    } ]}"""
    @Shared
    def queryFileSum = """{ "requests": [{
        "method": "$method",
        "path": {
            "base": "$path",
            "queries file": "$queriesFilePath",
            "queries": { "$queryKey2": "$queryVal2" }
        }
    }]}"""
    @Shared
    def header = """{"requests": [{
        "method": "$method",
        "path": "$path",
        "headers": { "$headerKey": "$headerVal"  }
    } ]}"""
    @Shared
    def headerPath = """{"requests": [{
        "method": "$method",
        "path": "$path",
        "headers file": "$headerFilePath"
    } ]}"""
    @Shared
    def headerPathOverride = """{"requests": [{
        "method": "$method",
        "path": "$path",
        "headers file": "$headerFilePath",
        "headers": { "$headerKey": "$headerVal2"  }
    } ]}"""
    @Shared
    def headerPathSum = """{ "requests": [{
        "method": "$method",
        "path": "$path",
        "headers file": "$headerFilePath",
        "headers": { "$headerKey2": "$headerVal2"  }
    } ] }"""
    @Shared
    def body = """{"requests": [{
        "method": "$method",
        "path": "$path",
        "bodyContains": "$bodyContent"
    } ]}"""
    @Shared
    def requestFileSimple = """{
        "requests": [ "$requestFilePath" ]
    }"""


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
        result.maxPort == DefaultValues.MOCK_SERVER_MAX_PORT
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
        result.maxPort == 1234
    }

    @Unroll
    def "should read request: #expRequest from #config"() {
        given:
        def reader = new StringConfigReader(config, [
                (requestFilePath): requestFileContent,
                (headerFilePath) : headerFileContent,
                (queriesFilePath): queriesFileContent
        ])
        when:
        def json = ConfigParser.getMainConfigJson(reader)
        def instance = new ConfigParser(reader)
        def result = instance.parseConfig(json)
        then:
        result.responses.size() == 1
        def requestParams = result.responses.get(0).key
        requestParams == expRequest
        where:
        config             | expRequest
        simplePath         | createRequest(false, '', [:], [:])
        basePath           | createRequest(false, '', [:], [:])
        patternPath        | createRequest(true, '', [:], [:])
        defaultQuery       | createRequest(false, '', [(queryKey): queryVal], DefaultValues.QUERY_MATCHING_METHOD, [:])
        exactQuery         | createRequest(false, '', [(queryKey): queryVal], MatchingMethod.EXACT, [:])
        queryFile          | createRequest(false, '', [(queryKey): queryVal], DefaultValues.QUERY_MATCHING_METHOD, [:])
        queryFileSum       | createRequest(false, '', [(queryKey): queryVal, (queryKey2): queryVal2], DefaultValues.QUERY_MATCHING_METHOD, [:])
        queryFileOverride  | createRequest(false, '', [(queryKey): queryVal2], DefaultValues.QUERY_MATCHING_METHOD, [:])
        header             | createRequest(false, '', [:], [(headerKey): headerVal])
        headerPath         | createRequest(false, '', [:], [(headerKey): headerVal])
        headerPathOverride | createRequest(false, '', [:], [(headerKey): headerVal2])
        headerPathSum      | createRequest(false, '', [:], [(headerKey): headerVal, (headerKey2): headerVal2])
        body               | createRequest(false, bodyContent, [:], [:])
        requestFileSimple  | createRequest(false, '', [:], [:])
    }

    def createRequest(boolean useRegexForPath,
                      String bodyMustContain,
                      Map<String, String> queries,
                      Map<String, String> headers) {
        return createRequest(useRegexForPath, bodyMustContain, queries, DefaultValues.QUERY_MATCHING_METHOD, headers)
    }

    def createRequest(boolean useRegexForPath,
                      String bodyMustContain,
                      Map<String, String> queries, MatchingMethod queriesMatchingMethod,
                      Map<String, String> headers) {
        return RequestParams.create(method, path, useRegexForPath, bodyMustContain, queries, queriesMatchingMethod, headers)
    }
}