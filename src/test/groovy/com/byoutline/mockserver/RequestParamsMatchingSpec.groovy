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
    def regexGet = '''{
      "method": "GET",
      "path": {
        "urlPattern": "/user.*"
      },
      "code": 200,
      "response file": "user.json"
    }'''
    @Shared
    def get = '''{
      "method": "GET",
      "path": "/user",
      "code": 204,
      "response": ""
    }'''
    @Shared
    def post = '''{
      "method": "POST",
      "path": {
        "base": "/user"
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putDefaultQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queries": { "username": "user" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putContainsQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "CONTAINS",
        "queries": { "username": "user" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putExactQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "EXACT",
        "queries": { "username": "user" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putNotContainsQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "NOT_CONTAINS",
        "queries": { "username": "user" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putDefaultRegexQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queries": { "username": "us.*" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putExactRegexQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "EXACT",
        "queries": { "username": "us.*" }
      },
      "code": 204,
      "response": ""
    }'''
    Request request = Mock()
    Path path = Mock()

    @Unroll
    def "should return #expResult for #method url: #url + with queries: #queries for json: #json"() {
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
        method | json                | url    | queries             | expResult
        "POST" | regexGet            | "/us"   | ''                  | false
        "GET"  | regexGet            | "/us"   | ''                  | false
        "GET"  | regexGet            | "/user" | ''                  | true
        "POST" | regexGet            | "/user" | ''                  | false
        "GET"  | regexGet            | "/user" | 'loginToken=1e324'  | true
        "GET"  | regexGet            | "/user" | 'a=1&b2&c=3&d=4'    | true
        "POST" | regexGet            | "/user" | 'loginToken=1e324'  | false

        "GET"  | get                 | "/us"   | ''                  | false
        "GET"  | get                 | "/user" | ''                  | true
        "GET"  | get                 | "/user" | 'a=1'               | true

        "POST" | post                | "/us"   | ''                  | false
        "GET"  | post                | "/us"   | ''                  | false
        "POST" | post                | "/user" | ''                  | true
        "POST" | post                | "/user" | 'loginToken=1e324'  | true

        "PUT"  | putDefaultQuery     | "/user" | ''                  | false
        "PUT"  | putDefaultQuery     | "/user" | 'loginToken=1e324'  | false
        "PUT"  | putDefaultQuery     | "/user" | 'username=abc'      | false
        "PUT"  | putDefaultQuery     | "/user" | 'user=user'         | false
        "PUT"  | putDefaultQuery     | "/user" | 'username=user'     | true
        "PUT"  | putDefaultQuery     | "/user" | 'username=user&a=1' | true

        "PUT"  | putContainsQuery    | "/user" | ''                  | false
        "PUT"  | putContainsQuery    | "/user" | 'loginToken=1e324'  | false
        "PUT"  | putContainsQuery    | "/user" | 'username=abc'      | false
        "PUT"  | putContainsQuery    | "/user" | 'user=user'         | false
        "PUT"  | putContainsQuery    | "/user" | 'username=user'     | true
        "PUT"  | putContainsQuery    | "/user" | 'username=user&a=1' | true

        "PUT"  | putExactQuery       | "/user" | ''                  | false
        "PUT"  | putExactQuery       | "/user" | 'loginToken=1e324'  | false
        "PUT"  | putExactQuery       | "/user" | 'username=abc'      | false
        "PUT"  | putExactQuery       | "/user" | 'user=user'         | false
        "PUT"  | putExactQuery       | "/user" | 'username=user'     | true
        "PUT"  | putExactQuery       | "/user" | 'username=user&a=1' | false

        "PUT"  | putNotContainsQuery | "/user" | ''                  | true
        "PUT"  | putNotContainsQuery | "/user" | 'loginToken=1e324'  | true
        "PUT"  | putNotContainsQuery | "/user" | 'username=abc'      | true
        "PUT"  | putNotContainsQuery | "/user" | 'user=user'         | true
        "PUT"  | putNotContainsQuery | "/user" | 'username=user'     | false
        "PUT"  | putNotContainsQuery | "/user" | 'username=user&a=1' | false

        "PUT"  | putDefaultRegexQuery| "/user" | 'username=u'        | false
        "PUT"  | putDefaultRegexQuery| "/user" | 'username=us'       | true
        "PUT"  | putDefaultRegexQuery| "/user" | 'username=user'     | true
        "PUT"  | putDefaultRegexQuery| "/user" | 'username=user&a=1' | true

        "PUT"  | putExactRegexQuery  | "/user" | 'username=u'        | false
        "PUT"  | putExactRegexQuery  | "/user" | 'username=us'       | true
        "PUT"  | putExactRegexQuery  | "/user" | 'username=user&a=1' | false
    }
}
