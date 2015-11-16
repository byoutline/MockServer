package com.byoutline.mockserver

import org.json.JSONObject
import org.simpleframework.http.Path
import org.simpleframework.http.Request
import org.simpleframework.http.parse.QueryParser
import spock.lang.Shared
import spock.lang.Unroll

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class RequestParamsMatchingSpec extends spock.lang.Specification {
    @Shared
    def jsonRegexGetCurrent = '''{
      "method": "GET",
      "path": {
        "urlPattern": "current.*"
      },
      "code": 200,
      "response file": "current.json"
    }'''
    @Shared
    def jsonPostCurrent = '''{
      "method": "POST",
      "path": {
        "base": "current"
      },
      "code": 204,
      "response": ""
    }'''
    Request request = Mock()
    Path path = Mock()

    @Unroll
    def "should match #method url: #url + with queries: #queries for json: #json"() {
        given:
        def jsonObj = new JSONObject(json)
        def params = ConfigParser.getPathFromJson(jsonObj)

        when:
        def result = params.matches(request)

        then:
        path.getPath() >> url
        request.getMethod() >> method
        request.getPath() >> path
        request.getNames() >> Collections.emptyList()
        request.getQuery() >> new QueryParser(queries)

        result == expResult

        where:
        method | json                | url       | queries            | expResult
        "POST" | jsonRegexGetCurrent | "cur"     | ''                 | false
        "GET"  | jsonRegexGetCurrent | "cur"     | ''                 | false
        "GET"  | jsonRegexGetCurrent | "current" | ''                 | true
        "POST" | jsonRegexGetCurrent | "current" | ''                 | false
        "GET"  | jsonRegexGetCurrent | "current" | 'loginToken=1e324' | true
        "GET"  | jsonRegexGetCurrent | "current" | 'a=1&b2&c=3&d=4'   | true
        "POST" | jsonRegexGetCurrent | "current" | 'loginToken=1e324' | false
        "POST" | jsonPostCurrent     | "cur"     | ''                 | false
        "GET"  | jsonPostCurrent     | "cur"     | ''                 | false
        "POST" | jsonPostCurrent     | "current" | ''                 | true
        "POST" | jsonPostCurrent     | "current" | 'loginToken=1e324' | false
    }
}
