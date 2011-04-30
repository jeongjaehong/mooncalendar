package org.nilriri.LunaCalendar.gcal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nilriri.LunaCalendar.dao.ScheduleBean;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.Prefs;

import android.os.Build;
import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;
import com.google.common.collect.Lists;

public class GoogleUtil {// extends Activity {

    private final List<CalendarEntry> calendars = Lists.newArrayList();
    private final List<EventEntry> events = Lists.newArrayList();
    private static HttpTransport transport;

    public GoogleUtil(String authToken) {
        initTransport();

        ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
        RedirectHandler.resetSessionId(transport);

        Log.d("XXXXXX", "GoogleLogin");

    }

    public void initTransport() {

        if (Build.VERSION.SDK_INT <= Common.FROYO) {
            transport = new ApacheHttpTransport();
        } else {
            transport = new NetHttpTransport();
        }
        GoogleHeaders headers = new GoogleHeaders();
        headers.setApplicationName("Google-CalendarAndroidSample/1.0");
        headers.gdataVersion = "2";
        transport.defaultHeaders = headers;
        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.DICTIONARY;
        transport.addParser(parser);

        Log.d("XXXXXX", "initTransport");

    }

    /*
        public void GoogleLogin(String authToken) throws IOException {
            initTransport();

            ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
            RedirectHandler.resetSessionId(transport);
            
            Log.d("XXXXXX", "GoogleLogin");

        }
    */

    public List<CalendarEntry> getCalendarList() throws IOException {
        Log.d("XXXXXX", "-------------getCalendarList Start-------------");
        String[] calendarNames;
        List<CalendarEntry> calendars = this.calendars;
        calendars.clear();
        //try {
        CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
        //CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
        // page through results
        while (true) {
            Log.d("XXXXXX", "forAllCalendarsFeed=" + url);

            CalendarFeed feed = CalendarFeed.executeGet(transport, url);
            if (feed.calendars != null) {
                calendars.addAll(feed.calendars);
            }
            String nextLink = feed.getNextLink();
            if (nextLink == null) {
                break;
            }
        }

        int numCalendars = calendars.size();
        calendarNames = new String[numCalendars];
        for (int i = 0; i < numCalendars; i++) {
            calendarNames[i] = calendars.get(i).title;
        }

        for (int i = 0; i < calendars.size(); i++) {
            Log.d(Common.TAG, "Calendars=" + calendars.get(i));
            //showEvents(calendars.get(i));
        }

        /* } catch (IOException e) {
             //handleException(e);
             calendarNames = new String[] { e.getMessage() };
             calendars.clear();
         }
         */
        Log.d("XXXXXX", "-------------getCalendarList End-------------");

        return calendars;
        //return calendarNames;

    }

    public List<EventEntry> getEvents(CalendarEntry calendar) throws IOException {

        String[] eventNames;
        List<EventEntry> events = this.events;
        events.clear();
        try {
            CalendarUrl url = new CalendarUrl(calendar.getEventFeedLink());
            while (true) {

                Log.d("XXXXXX", "calendar.getSelfEventFeedLink()=" + url);

                EventFeed feed = EventFeed.executeGet(transport, url);

                if (feed.events != null) {
                    events.addAll(feed.events);
                }
                String nexturl = feed.getNextLink();
                if (nexturl == null) {
                    break;
                } else {
                    url = new CalendarUrl(nexturl);
                }
            }
            
            int numCalendars = events.size();
            eventNames = new String[numCalendars];
            for (int i = 0; i < numCalendars; i++) {
                eventNames[i] = events.get(i).title;
                Log.d(Common.TAG, "events.get(i).who.size()=" + events.get(i).who.size());
            }

            for (int i = 0; i < eventNames.length; i++)
                Log.d(Common.TAG, "events=" + eventNames[i]);
           
        } catch (IOException e) {
            //handleException(e);
            // eventNames = new String[] { e.getMessage() };
            events.clear();
        }

        return events;

    }

    public void deleteEvent(String account, String uid) throws IOException {

        //       https://www.google.com/calendar/feeds/default/private/full/

        CalendarUrl eventEditUrl = new CalendarUrl(CalendarUrl.ROOT_URL);
        EventEntry event = new EventEntry();
        List<Link> links = new ArrayList<Link>();
        
        String editUrl = eventEditUrl.forDeleteEventFeed(account, uid).build();        
        
        Link link = new Link();
        link.href = editUrl;
        link.rel = "edit";
        
        

        Log.d("XXXXXX", "delete url=" + link.href);
        

        links.add(link);

        event.links = links;
        event.id = editUrl;
        
        links = event.links;
        for(int i=0;i<links.size();i++)
            Log.d("XXXXXX", "links("+i+")=" + links.get(i).href);

        event.executeDelete(transport);

    }

    public EventEntry addEvent(CalendarEntry calendar, ScheduleBean scheduleBean) throws IOException {
        CalendarUrl url = new CalendarUrl(calendar.getEventFeedLink());
        EventEntry event = newEvent(scheduleBean);

        // 등록된 결과를 리턴받는다.
        return event.executeInsert(transport, url);

    }

    private EventEntry newEvent(ScheduleBean scheduleBean) {
        EventEntry event = new EventEntry();

        When when = new When();
        when.startTime = Common.toDateTime(scheduleBean.getDate());
        when.endTime = Common.toDateTime(scheduleBean.getDate());

        //WhoList whoList = new WhoList();
        //whoList.add("", "", "");

        List<Who> whoList = new ArrayList<Who>();
        Who who = new Who();
        who.email = "gyjeong@gmail.com";
        who.rel = "frendly";
        who.valueString = "정갑용";
        whoList.add(who);

        event.title = scheduleBean.getTitle();
        event.when = when;
        //event.who = whoList.getList();
        //event.who = whoList;
        event.content = scheduleBean.getContents();
        //event.recurrence = "";

        return event;
    }

    private void batchAddEvents(CalendarEntry calendar, ScheduleBean scheduleBean) throws IOException {
        EventFeed feed = new EventFeed();
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            EventEntry event = newEvent(scheduleBean);
            event.batchId = Integer.toString(i);
            event.batchOperation = BatchOperation.INSERT;
            feed.events.add(event);
        }
        EventFeed result = feed.executeBatch(transport, calendar);
        for (EventEntry event : result.events) {
            BatchStatus batchStatus = event.batchStatus;
            if (batchStatus != null && !HttpResponse.isSuccessStatusCode(batchStatus.code)) {
                System.err.println("Error posting event: " + batchStatus.reason);
            }
        }
    }

}
