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
    def deleteExactRegexQuery = '''{
      "method": "DELETE",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "EXACT",
        "queries": { "id": "[0-9][0-9][0-9][0-9]" }
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
        "queries": { "username": "us.*","pass": "[a-h][0-9]+[a-z]" }
      },
      "code": 204,
      "response": ""
    }'''
    @Shared
    def putEmptyExactQuery = '''{
      "method": "PUT",
      "path": {
        "base": "/user",
        "queriesMatchingMethod": "EXACT",

      },
      "code": 204,
      "response": ""
    }'''


    Request request = Mock()
    Path path = Mock()

    @Unroll
    def "should return #expResult for #method , #url , #queries for jsonConfig: #jsonConfig"() {
        given:
        def jsonObj = new JSONObject(jsonConfig)
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
        method   | url     | queries                       | jsonConfig            | expResult
        "POST"   | "/us"   | ''                            | regexGet              | false
        "GET"    | "/us"   | ''                            | regexGet              | false
        "GET"    | "/user" | ''                            | regexGet              | true
        "POST"   | "/user" | ''                            | regexGet              | false
        "GET"    | "/user" | 'loginToken=1e324'            | regexGet              | true
        "GET"    | "/user" | 'a=1&b2&c=3&d=4'              | regexGet              | true
        "POST"   | "/user" | 'loginToken=1e324'            | regexGet              | false

        "GET"    | "/us"   | ''                            | get                   | false
        "GET"    | "/user" | ''                            | get                   | true
        "GET"    | "/user" | 'a=1'                         | get                   | true

        "POST"   | "/us"   | ''                            | post                  | false
        "GET"    | "/us"   | ''                            | post                  | false
        "POST"   | "/user" | ''                            | post                  | true
        "POST"   | "/user" | 'loginToken=1e324'            | post                  | true

        "DELETE" | "/us"   | ''                            | deleteExactRegexQuery | false
        "DELETE" | "/user" | ''                            | deleteExactRegexQuery | false
        "DELETE" | "/user" | 'id=1324'                     | deleteExactRegexQuery | true
        "DELETE" | "/user" | 'loginToken=1e324'            | deleteExactRegexQuery | false

        "PUT"    | "/user" | ''                            | putDefaultQuery       | false
        "PUT"    | "/user" | 'loginToken=1e324'            | putDefaultQuery       | false
        "PUT"    | "/user" | 'username=abc'                | putDefaultQuery       | false
        "PUT"    | "/user" | 'user=user'                   | putDefaultQuery       | false
        "PUT"    | "/user" | 'username=user'               | putDefaultQuery       | true
        "PUT"    | "/user" | 'username=user&a=1'           | putDefaultQuery       | true

        "PUT"    | "/user" | ''                            | putContainsQuery      | false
        "PUT"    | "/user" | 'loginToken=1e324'            | putContainsQuery      | false
        "PUT"    | "/user" | 'username=abc'                | putContainsQuery      | false
        "PUT"    | "/user" | 'user=user'                   | putContainsQuery      | false
        "PUT"    | "/user" | 'username=user'               | putContainsQuery      | true
        "PUT"    | "/user" | 'username=user&a=1'           | putContainsQuery      | true

        "PUT"    | "/user" | ''                            | putExactQuery         | false
        "PUT"    | "/user" | 'loginToken=1e324'            | putExactQuery         | false
        "PUT"    | "/user" | 'username=abc'                | putExactQuery         | false
        "PUT"    | "/user" | 'user=user'                   | putExactQuery         | false
        "PUT"    | "/user" | 'username=user'               | putExactQuery         | true
        "PUT"    | "/user" | 'username=user&a=1'           | putExactQuery         | false

        "PUT"    | "/user" | ''                            | putNotContainsQuery   | true
        "PUT"    | "/user" | 'loginToken=1e324'            | putNotContainsQuery   | true
        "PUT"    | "/user" | 'username=abc'                | putNotContainsQuery   | true
        "PUT"    | "/user" | 'user=user'                   | putNotContainsQuery   | true
        "PUT"    | "/user" | 'username=user'               | putNotContainsQuery   | false
        "PUT"    | "/user" | 'username=user&a=1'           | putNotContainsQuery   | false

        "PUT"    | "/user" | 'username=u'                  | putDefaultRegexQuery  | false
        "PUT"    | "/user" | 'username=us'                 | putDefaultRegexQuery  | true
        "PUT"    | "/user" | 'username=user'               | putDefaultRegexQuery  | true
        "PUT"    | "/user" | 'username=user&a=1'           | putDefaultRegexQuery  | true

        "PUT"    | "/user" | 'username=u'                  | putExactRegexQuery    | false
        "PUT"    | "/user" | 'username=us'                 | putExactRegexQuery    | false
        "PUT"    | "/user" | 'username=user&a=1'           | putExactRegexQuery    | false
        "PUT"    | "/user" | 'username=use&pass=a443a'     | putExactRegexQuery    | true
        "PUT"    | "/user" | 'username=use&password=a443a' | putExactRegexQuery    | false
        "PUT"    | "/user" | 'username=userAdmin&pass=a4h' | putExactRegexQuery    | true

        "PUT"    | "/user" | 'username=use'                | putEmptyExactQuery    | false
        "PUT"    | "/user" | ''                            | putEmptyExactQuery    | true
        "PUT"    | "/user" | '    '                        | putEmptyExactQuery    | false
        "PUT"    | "/us"   | ''                            | putEmptyExactQuery    | false
    }
}
