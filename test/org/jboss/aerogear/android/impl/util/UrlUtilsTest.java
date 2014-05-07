/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.impl.util;

import org.junit.Test;

import java.net.URL;

import static org.jboss.aerogear.android.impl.util.UrlUtils.appendQueryToBaseURL;
import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;
import static org.junit.Assert.assertEquals;

public class UrlUtilsTest {

    @Test
    public void testAppendToBaseURLWithBothHaveSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/");
        String endpoint = "/endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testAppendToBaseURLWithOnlyBaseURLHasSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/");
        String endpoint = "endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testEmptyEndpointDoesNotAlterBaseUrl() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/path");
        String endpoint = "";
        URL expectURL = new URL("http://fakeurl.com/path");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testAppendToBaseURLWithOnlyEndpointHasSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com");
        String endpoint = "/endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testAppendToBaseURLWithBothWithoutSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com");
        String endpoint = "endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testAppendQueryWithEmptyQuery() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/endpoint");
        String query = "";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendQueryToBaseURL(baseURL, query));
    }

    @Test
    public void testAppendQueryWithUrlEndSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/endpoint/");
        String query = "";
        URL expectURL = new URL("http://fakeurl.com/endpoint/");

        assertEquals(expectURL, appendQueryToBaseURL(baseURL, query));
    }

    @Test
    public void testAppendQueryWithQueryStartWithQuestionMark() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/endpoint");
        String query = "?y=2";
        URL expectURL = new URL("http://fakeurl.com/endpoint?y%3D2");

        assertEquals(expectURL, appendQueryToBaseURL(baseURL, query));
    }

    @Test
    public void testAppendQueryWithParameter() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/endpoint?x=1");
        String query = "y=2";
        URL expectURL = new URL("http://fakeurl.com/endpoint?x=1?y%3D2");

        assertEquals(expectURL, appendQueryToBaseURL(baseURL, query));
    }

    @Test
    public void testAppendQueryWithBaseURLEndWithQuestionMarkAndQueryStartWithQuestionMark() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/endpoint?x=1");
        String query = "?y=2";
        URL expectURL = new URL("http://fakeurl.com/endpoint?x=1?y%3D2");

        assertEquals(expectURL, appendQueryToBaseURL(baseURL, query));
    }

}
