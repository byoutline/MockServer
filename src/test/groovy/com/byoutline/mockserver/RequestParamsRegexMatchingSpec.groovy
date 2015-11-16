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
class RequestParamsRegexMatchingSpec extends spock.lang.Specification {
    @Shared
    def jsonRegexCurrent = '''{
      "method": "GET",
      "path": {
        "urlPattern": "current.*"
      },
      "code": 200,
      "response file": "current.json"
    }'''
    Request request = Mock()
    Path path = Mock()

    @Unroll
    def "should match #method url: #url + with queries: #queries"() {
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
        method | json             | url       | queries            | expResult
        "POST" | jsonRegexCurrent | "cur"     | ''                 | false
        "GET"  | jsonRegexCurrent | "current" | ''                 | true
        "POST" | jsonRegexCurrent | "current" | ''                 | false
        "GET"  | jsonRegexCurrent | "current" | 'loginToken=1e324' | true
        "GET"  | jsonRegexCurrent | "current" | 'a=1&b2&c=3&d=4'   | true
        "POST" | jsonRegexCurrent | "current" | 'loginToken=1e324' | false
    }
}
