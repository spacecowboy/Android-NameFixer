package com.spacecowboy.contacts;

import com.spacecowboy.contacts.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class NameFixer extends Activity {

	private ListView	contactList;
	private Button		okButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		contactList = (ListView) findViewById(R.id.contactList);
		okButton = (Button) findViewById(R.id.OK);

		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				changeSorting();
			}
		});

		populateContactList();
	}
	
	/**
     * Populate the contact list based on Exchange
     */
    private void populateContactList() {
        // Build adapter with contact entries
        Cursor cursor = getContacts();
        String[] fields = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        };
        //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor,
        //        fields, new int[] {R.id.contactEntryDisplayName, R.id.contactEntryFamilyName, R.id.contactEntryGivenName});
        //contactList.setAdapter(adapter);
    }
    
    /**
     * Obtains the contact list for Exchange
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getContacts()
    {
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
                ("0") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    }

	protected void changeSorting() {

	}
}