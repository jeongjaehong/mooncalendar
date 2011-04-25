package org.nilriri.LunaCalendar.tools;

import org.nilriri.LunaCalendar.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.PeopleColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ContactSelector extends Activity {
    public static final int PICK_CONTACT = 1;
    private Button btnContacts;
    private TextView txtContacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_selector);

        btnContacts = (Button) findViewById(R.id.btn_contacts);
        txtContacts = (TextView) findViewById(R.id.txt_contacts);

        btnContacts.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                //Intent intent = new Intent(Intent.ACTION_PICK, Contacts.People.CONTENT_URI);   
                //Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);   
                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI);
                //ContactMethodsColumns.KIND
                startActivityForResult(intent, PICK_CONTACT);
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        // String name = c.getString(c.getColumnIndexOrThrow(Contacts.People. .NAME));   
                        String name = c.getString(c.getColumnIndexOrThrow(PeopleColumns.NAME));
                        //   txtContacts.setText(name);   
                    }
                }
                break;
        }
    }
}
