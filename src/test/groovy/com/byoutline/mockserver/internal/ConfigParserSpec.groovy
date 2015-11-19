package com.byoutline.mockserver.internal

import com.byoutline.mockserver.DefaultValues
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class ConfigParserSpec extends Specification {
    @Shared
    def path = '/a'
    @Shared
    def method = 'GET'
    @Shared
    def queryKey = 'username'
    @Shared
    def queryVal = 'user'
    @Shared
    def headerKey = 'sessionId'
    @Shared
    def headerVal = '1234'
    @Shared
    def bodyContent = 'body content'

    @Shared
    def response = 'resp'
    @Shared
    def jsonResponse = '["json"]'
    @Shared
    def responseFilePath = '/responseFile.json'
    @Shared
    def requestFilePath = '/request.json'
    @Shared
    def requestFileWithResponseFilePath = '/requestWithRequestFile.json'
    @Shared
    def requestFileContent = """{
                    "method": "$method",
                    "path": "$path"
                }"""
    @Shared
    def requestFileWithResponseFileContent = """{
                    "method": "$method",
                    "path": "$path",
                    "response file": "$responseFilePath"
                }"""

    @Shared
    def simplePath = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path"
                }
            ]
        }"""
    @Shared
    def basePath = """{
            "requests": [
                {
                    "method": "$method",
                    "path": { "base": "$path" }
                }
            ]
        }"""
    @Shared
    def patternPath = """{
            "requests": [
                {
                    "method": "$method",
                    "path": { "urlPattern": "$path" }
                }
            ]
        }"""
    @Shared
    def defaultQuery = """{
            "requests": [
                {
                    "method": "$method",
                    "path": {
                        "base": "$path",
                        "queries": { "$queryKey": "$queryVal" }
                    },
                }
            ]
        }"""
    @Shared
    def exactQuery = """{
            "requests": [
                {
                    "method": "$method",
                    "path": {
                        "base": "$path",
                        "queriesMatchingMethod": "EXACT",
                        "queries": { "$queryKey": "$queryVal" }
                    },
                }
            ]
        }"""
    @Shared
    def header = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "headers": { "$headerKey": "$headerVal"  }
                }
            ]
        }"""
    @Shared
    def body = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "bodyContains": "$bodyContent"
                }
            ]
        }"""
    @Shared
    def simpleResponse = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "response": "$response"
                }
            ]
        }"""
    @Shared
    def responseCode = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "code": "213"
                }
            ]
        }"""
    @Shared
    def responseHeaders = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "response headers": { "$headerKey": "$headerVal"  }
                }
            ]
        }"""
    @Shared
    def responseFile = """{
            "requests": [
                {
                    "method": "$method",
                    "path": "$path",
                    "response file": "$responseFilePath"
                }
            ]
        }"""
    @Shared
    def requestFileSimple = """{
            "requests": [ "$requestFilePath" ]
        }"""
    @Shared
    def requestFileWithResponseFile = """{
            "requests": [ "$requestFileWithResponseFilePath" ]
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

    @Unroll
    def "should read request: #expRequest from #config"() {
        given:
        def reader = new StringConfigReader(config, [(requestFilePath): requestFileContent])
        when:
        def json = ConfigParser.getMainConfigJson(reader)
        def instance = new ConfigParser(reader)
        def result = instance.parseConfig(json)
        then:
        result.responses.size() == 1
        def requestParams = result.responses.get(0).key
        requestParams == expRequest
        where:
        config            | expRequest
        simplePath        | createRequest(false, '', [:], [:])
        basePath          | createRequest(false, '', [:], [:])
        patternPath       | createRequest(true, '', [:], [:])
        defaultQuery      | createRequest(false, '', [(queryKey): queryVal], DefaultValues.QUERY_MATCHING_METHOD, [:])
        exactQuery        | createRequest(false, '', [(queryKey): queryVal], MatchingMethod.EXACT, [:])
        header            | createRequest(false, '', [:], [(headerKey): headerVal])
        body              | createRequest(false, bodyContent, [:], [:])
        requestFileSimple | createRequest(false, '', [:], [:])
    }

    @Unroll
    def "should read response: #expResponse from #config"() {
        given:
        def reader = new StringConfigReader(config, [(responseFilePath): jsonResponse, (requestFileWithResponseFilePath): requestFileWithResponseFileContent])
        when:
        def json = ConfigParser.getMainConfigJson(reader)
        def instance = new ConfigParser(reader)
        def result = instance.parseConfig(json)
        then:
        result.responses.size() == 1
        def requestParams = result.responses.get(0).value
        requestParams == expResponse
        where:
        config                      | expResponse
        simpleResponse              | ResponseParams.create(response, false, [:])
        responseCode                | ResponseParams.create(213, 'OK', false, [:])
        responseHeaders             | ResponseParams.create('OK', false, [(headerKey): headerVal])
        responseFile                | ResponseParams.create(jsonResponse, false, [:])
        requestFileWithResponseFile | ResponseParams.create(jsonResponse, false, [:])
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