/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nilriri.LunaCalendar.gcal;

import java.io.IOException;

import org.nilriri.LunaCalendar.tools.Common;

import android.util.Log;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;

/**
 * @author Yaniv Inbar
 */
public class RedirectHandler {

    /**
     * See <a
     * href="http://code.google.com/apis/calendar/faq.html#redirect_handling">How
     * do I handle redirects...?</a>.
     */
    private static class SessionIntercepter implements HttpExecuteIntercepter {

        private String gsessionid;

        private SessionIntercepter(HttpTransport transport, GoogleUrl locationUrl) {
            this.gsessionid = (String) locationUrl.getFirst("gsessionid");
            transport.removeIntercepters(SessionIntercepter.class);
            transport.intercepters.add(0, this); // must be first
        }

        public void intercept(HttpRequest request) {
            request.url.set("gsessionid", this.gsessionid);
        }
    }

    /** Resets the session ID stored for the HTTP transport. */
    public static void resetSessionId(HttpTransport transport) {
        transport.removeIntercepters(SessionIntercepter.class);
    }

    static HttpResponse execute(HttpRequest request) throws IOException {
        try {
            //HttpResponse res = request.execute();
            //Log.d(Common.TAG, "RedirectHandler response=" + res.parseAsString());
            //return res;
            return request.execute();
        } catch (HttpResponseException e) {
            if (e.response.statusCode == 302) {

                GoogleUrl url = new GoogleUrl(e.response.headers.location);
                request.url = url;

                Log.d(Common.TAG, "302 url=" + url);

                new SessionIntercepter(request.transport, url);
                e.response.ignore(); // force the connection to close

                try {
                    HttpResponse res = request.execute();
                    return res;
                } catch (HttpResponseException re) {

                    if (re.response.statusCode == 403) {

                        Log.d(Common.TAG, "403 request.headers =" + request.headers.toString());
                        Log.d(Common.TAG, "403 request.method =" + request.method);
                        Log.d(Common.TAG, "403 request.content =" + request.content);
                        Log.d(Common.TAG, "403 response.headers =" + e.response.headers.toString());
                        Log.d(Common.TAG, "403 response =" + e.response.parseAsString());

                        return re.response;

                    } else {
                        re.printStackTrace();
                        throw re;
                    }

                }

            } else {

                e.printStackTrace();

                throw e;
            }
        }

    }
}
