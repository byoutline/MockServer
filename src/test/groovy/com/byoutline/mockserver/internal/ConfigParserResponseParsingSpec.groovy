package com.byoutline.mockserver.internal

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class ConfigParserResponseParsingSpec extends Specification {
    @Shared
    def path = '/a'
    @Shared
    def method = 'GET'
    @Shared
    def headerKey = 'sessionId'
    @Shared
    def headerVal = '1234'

    @Shared
    def response = 'resp'
    @Shared
    def jsonResponse = '["json"]'
    @Shared
    def responseFilePath = '/responseFile.json'

    @Shared
    def requestFileWithResponseFilePath = '/requestWithRequestFile.json'

    @Shared
    def requestFileWithResponseFileContent = """{
                    "method": "$method",
                    "path": "$path",
                    "response file": "$responseFilePath"
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
    def requestFileWithResponseFile = """{
            "requests": [ "$requestFileWithResponseFilePath" ]
        }"""

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
}