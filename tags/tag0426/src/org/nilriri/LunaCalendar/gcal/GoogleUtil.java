package org.nilriri.LunaCalendar.gcal;

import java.io.IOException;
import java.util.List;

import org.nilriri.LunaCalendar.tools.Common;

import android.os.Build;
import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;
import com.google.common.collect.Lists;

public class GoogleUtil {// extends Activity {

    private final List<CalendarEntry> calendars = Lists.newArrayList();
    private final List<EventEntry> events = Lists.newArrayList();
    private HttpTransport transport;
    
    public GoogleUtil(String authToken){
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

    public  List<EventEntry> getEvents(CalendarEntry calendar) throws IOException {

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
            eventNames = new String[] { e.getMessage() };
            events.clear();
        }

        return events;

    }

}
