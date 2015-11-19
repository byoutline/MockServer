package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.TestConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RequestParamsTest {

    public static final String NAME = "name";
    public static final String VALUE = "value";

    @Mock
    private Query query;
    @Mock
    private Path path;
    @Mock
    private Request request;

    private Map<String, String> headers = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(request.getQuery()).thenReturn(query);
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(TestConstants.METHOD);
    }

    @Test
    public void shouldFailOnMissingHeader() throws Exception {
        //given
        headers = Collections.singletonMap("name", "value");
        RequestParams requestParams = RequestParams.create(TestConstants.METHOD, TestConstants.TEST_PATH, false, "",
                Collections.<String, String>emptyMap(), MatchingMethod.CONTAINS, headers);

        //when
        when(request.getNames()).thenReturn(Collections.<String>emptyList());
        when(query.keySet()).thenReturn(Collections.<String>emptySet());
        when(path.getPath()).thenReturn(TestConstants.TEST_PATH);

        //then
        assertThat(requestParams.matches(request)).isFalse();
    }

    @Test
    public void shouldCheckMethod() throws Exception {
        RequestParams requestParams = RequestParams.create(TestConstants.METHOD, TestConstants.TEST_PATH, false, "",
                Collections.<String, String>emptyMap(), MatchingMethod.CONTAINS, Collections.<String, String>emptyMap());

        //when
        when(request.getNames()).thenReturn(Collections.<String>emptyList());
        when(query.keySet()).thenReturn(Collections.<String>emptySet());
        when(path.getPath()).thenReturn(TestConstants.TEST_PATH);

        //then
        assertThat(requestParams.matches(request)).isTrue();
    }

    @Test
    public void shouldCheckHeader() throws Exception {
        //given
        headers = Collections.singletonMap(NAME, VALUE);
        RequestParams requestParams = RequestParams.create(TestConstants.METHOD, TestConstants.TEST_PATH, false, "",
                Collections.<String, String>emptyMap(), MatchingMethod.CONTAINS, headers);

        //when
        when(request.getNames()).thenReturn(Collections.singletonList(NAME));
        when(request.getValue(NAME)).thenReturn(VALUE);
        when(query.keySet()).thenReturn(Collections.<String>emptySet());
        when(path.getPath()).thenReturn(TestConstants.TEST_PATH);

        //then
        assertThat(requestParams.matches(request)).isTrue();
    }

    @Test
    public void shouldFailOnOneMissingHeader() throws Exception {
        //given
        headers = new HashMap<String, String>();
        headers.put(NAME, VALUE);
        headers.put("missing", VALUE);
        RequestParams requestParams = RequestParams.create(TestConstants.METHOD, TestConstants.TEST_PATH, false, "",
                Collections.<String, String>emptyMap(), MatchingMethod.CONTAINS, headers);

        //when
        when(request.getNames()).thenReturn(Collections.singletonList(NAME));
        when(request.getValue(NAME)).thenReturn(VALUE);
        when(query.keySet()).thenReturn(Collections.<String>emptySet());
        when(path.getPath()).thenReturn(TestConstants.TEST_PATH);

        //then
        assertThat(requestParams.matches(request)).isFalse();
    }

    @Test
    public void shouldFailOnOneWrongValueOfHeader() throws Exception {
        //given
        headers.put(NAME, VALUE);
        RequestParams requestParams = RequestParams.create(TestConstants.METHOD, TestConstants.TEST_PATH, false, "",
                Collections.<String, String>emptyMap(), MatchingMethod.CONTAINS, headers);

        //when
        when(request.getNames()).thenReturn(Collections.singletonList(NAME));
        when(request.getValue(NAME)).thenReturn("wrong");
        when(query.keySet()).thenReturn(Collections.<String>emptySet());
        when(path.getPath()).thenReturn(TestConstants.TEST_PATH);

        //then
        assertThat(requestParams.matches(request)).isFalse();
    }
}