/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nilriri.LunaCalendar.gcal;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

public abstract class Feed {

    @Key("link")
    public List<Link> links;

    public String getNextLink() {
        return Link.find(links, "next");
    }

    public String getBatchLink() {
        return Link.find(links, "http://schemas.google.com/g/2005#batch");
    }

    public abstract List<? extends Entry> getEntries();

    static Feed executeGet(HttpTransport transport, CalendarUrl url, Class<? extends Feed> feedClass) throws IOException {
        url.fields = GoogleAtom.getFieldsFor(feedClass);
        HttpRequest request = transport.buildGetRequest();
        request.url = url;

        HttpResponse response = RedirectHandler.execute(request);
        //Log.d("~~~~~~~~~~~~~~~~", "response.toString()=" + response.parseAsString());

        return response.parseAs(feedClass);

        // return RedirectHandler.execute(request).parseAs(feedClass);
    }

}
