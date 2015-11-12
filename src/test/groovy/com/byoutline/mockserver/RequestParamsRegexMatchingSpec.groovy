package com.byoutline.mockserver

import org.json.JSONObject
import org.simpleframework.http.Path
import org.simpleframework.http.Query
import org.simpleframework.http.Request
import spock.lang.Shared
import spock.lang.Unroll

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class RequestParamsRegexMatchingSpec extends spock.lang.Specification {
    @Shared
    def current = '''{
      "method": "GET",
      "path": {
        "urlPattern": "current.*"
      },
      "code": 200,
      "response file": "current.json"
    }'''
    Request request = Mock()
    Path path = Mock()
    Query query = Mock()

    @Unroll
    def "should match url: #url"() {
        given:
        def jsonObj = new JSONObject(json)
        def params = ConfigParser.getPathFromJson(jsonObj)

        when:
        def result = params.matches(request)

        then:
        path.getPath() >> url
        query.keySet() >> queries.keySet()
        query.entrySet() >> queries.entrySet()
        request.getMethod() >> method
        request.getPath() >> path
        request.getNames() >> Collections.emptyList()
        request.getQuery() >> query

        result == expResult

        where:
        method | json       | url       | queries                | expResult
        "POST" | current    | "cur"     | [:]                    | false
        "GET"  | current    | "current" | [:]                    | true
        "POST" | current    | "current" | [:]                    | false
        "GET"  | current    | "current" | ['loginToken':'1e324'] | false
    }


}
