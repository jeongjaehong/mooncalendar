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

import java.util.ArrayList;
import java.util.List;

import org.nilriri.LunaCalendar.tools.Common;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

public class When {

    @Key("@startTime")
    public DateTime startTime;

    @Key("@endTime")
    public DateTime endTime;

    @Key("gd:reminder")
    public List<Reminder> reminder = new ArrayList<Reminder>();

    public When() {
        
    }

    public When(String str) {
        try {
            String whenStr[] = Common.tokenFn(str, ",");
            for (int i = 0; i < whenStr.length; i++) {
                String whenItem[] = Common.tokenFn(whenStr[i], ";");
                if ("startTime".equals(whenItem[0])) {
                    this.startTime = Common.toDateTime(whenItem[1],true);
                } else if ("endTime".equals(whenItem[0])) {
                    this.endTime = Common.toDateTime(whenItem[1],true);
                } else if ("reminder".equals(whenItem[0])) {
                    String remainstr[] = Common.tokenFn(whenItem[1], "*");
                    for (int j = 0; j < remainstr.length; j++) {
                        reminder.add(new Reminder(remainstr[j]));
                    }
                }
            }
        } catch (Exception e) {
            Log.d("^^^^^^^^^^^", " When Exception =" + e.getMessage());
            startTime = Common.toDateTime("",true);
            endTime = Common.toDateTime("",true);
            reminder = new ArrayList<Reminder>();
        }
    }

    public String parseAsString() {
        StringBuilder result = new StringBuilder();

        result.append("startTime;").append(startTime).append(",");
        result.append("endTime;").append(endTime);

        for (int i = 0; i < this.reminder.size(); i++) {
            if (i == 0) {
                result.append(",reminder;").append(this.reminder.get(i).parseAsString());
            } else {
                result.append("*").append(this.reminder.get(i).parseAsString());
            }
        }
        return result.toString();
    }

}
