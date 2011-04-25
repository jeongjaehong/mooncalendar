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

import org.nilriri.LunaCalendar.tools.Common;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class EventEntry extends Entry {

    @Key("gd:when")
    public When when;

    @Key("gd:who")
    //public Who who;
    public List<Who> who;// = new ArrayList<Who>();

    @Key("gd:recurrence")
    public String recurrence;

    @Key("content")
    public String content;

    @Key("published")
    public String published;

    @Key("batch:id")
    public String batchId;

    @Key("batch:status")
    public BatchStatus batchStatus;

    @Key("batch:operation")
    public BatchOperation batchOperation;

    @Key("gCal:uid")
    public String uid;

    @Override
    public EventEntry clone() {
        return (EventEntry) super.clone();
    }

    @Override
    public EventEntry executeInsert(HttpTransport transport, CalendarUrl url) throws IOException {
        return (EventEntry) super.executeInsert(transport, url);
    }

    public EventEntry executePatchRelativeToOriginal(HttpTransport transport, CalendarEntry original) throws IOException {
        return (EventEntry) super.executePatchRelativeToOriginal(transport, original);
    }

    /*   */
    public String getStartDate() {
        if (when != null) {
            return when.startTime.toString().substring(0, 10);
        } else {
            String values[] = Common.tokenFn(recurrence, "\n");
            String keys[] = Common.tokenFn(values[0], ":");
            return Common.fmtDate(keys[1]);
        }
    }

    public String getEndDate() {
        if (when != null) {
            return when.endTime.toString().substring(0, 10);
        } else {
            String values[] = Common.tokenFn(recurrence, "\n");
            String keys[] = Common.tokenFn(values[1], ":");
            return Common.fmtDate(keys[1]);
        }
    }

}
