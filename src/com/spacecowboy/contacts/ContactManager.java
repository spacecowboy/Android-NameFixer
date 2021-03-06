/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spacecowboy.contacts;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * This application reads all the contacts from the selected account, or all contacts if no account is selected.
 * The user can then choose what display name to assign them.
 * @author Jonas Kalderstam
 *
 */
public final class ContactManager extends Activity implements
		OnAccountsUpdateListener {

	public static final String		TAG				= "ContactManager";

	private Button					mFixNamesButton;
	private ListView				mContactList;
	private CheckBox				mFirstLastControl;
	private boolean					mFirstLast;
	private CheckBox				mCommaControl;
	private boolean					mComma;
	private AccountAdapter			mAccountAdapter;
	private Spinner					mAccountSpinner;
	private AccountData				mSelectedAccount;
	private ArrayList<AccountData>	mAccounts;
	private ArrayList<Contact>		contacts;

	public static final String		ACCOUNT_NAME	= "com.example.android.contactmanager.ContactsAdder.ACCOUNT_NAME";
	public static final String		ACCOUNT_TYPE	= "com.example.android.contactmanager.ContactsAdder.ACCOUNT_TYPE";

	/**
	 * Called when the activity is first created. Responsible for initializing
	 * the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_manager);

		contacts = new ArrayList<Contact>();

		// Obtain handles to UI objects
		mAccountSpinner = (Spinner) findViewById(R.id.accountSpinner);
		mFixNamesButton = (Button) findViewById(R.id.fixNamesButton);
		mContactList = (ListView) findViewById(R.id.contactList);

		// Initialize class properties
		mFirstLast = true;
		mFirstLastControl = (CheckBox) findViewById(R.id.firstlastcheck);
		mFirstLastControl.setChecked(mFirstLast);
		mFirstLastControl
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.d(TAG, "mFirstLastControl changed: " + isChecked);
						mFirstLast = isChecked;
						populateContactList();
					}
				});

		mComma = false;
		mCommaControl = (CheckBox) findViewById(R.id.commacheck);
		mCommaControl.setChecked(mComma);
		mCommaControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.d(TAG, "mCommaControl changed: " + isChecked);
				mComma = isChecked;
				populateContactList();
			}
		});

		// Register handler for UI elements
		mFixNamesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "mAddAccountButton clicked");
				fixNames();
			}
		});

		// Prepare model for account spinner
		mAccounts = new ArrayList<AccountData>();
		mAccountAdapter = new AccountAdapter(this, mAccounts);
		mAccountSpinner.setAdapter(mAccountAdapter);

		// Prepare the system account manager. On registering the listener
		// below, we also ask for
		// an initial callback to pre-populate the account list.
		AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);

		// Register handlers for UI elements
		mAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long i) {
				updateAccountSelection();
				populateContactList();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			// We don't need to worry about nothing being selected, since
			// Spinners don't allow
			// this.
			}
		});

		// Populate the contact list here as well, just in case no accounts exists.
		populateContactList();
	}

	/**
	 * Populate the contact list based on account currently selected in the
	 * account spinner.
	 */
	private void populateContactList() {
		// clear contactlist
		contacts.clear();
		int layout;
		if (mFirstLast) {
			if (mComma)
				layout = R.layout.contact_entry_firstlast_comma;
			else
				layout = R.layout.contact_entry_firstlast;
		}
		else {
			if (mComma)
				layout = R.layout.contact_entry_lastfirst_comma;
			else
				layout = R.layout.contact_entry_lastfirst;
		}
		// Build adapter with contact entries
		Cursor cursor = getContacts();
		String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME };
		// try {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, layout,
				cursor, fields, new int[] { R.id.contactEntryText,
						R.id.contactEntryFamily, R.id.contactEntryGiven });
		mContactList.setAdapter(adapter);
		// }
		// catch (Exception e) {
		// String error = e.getLocalizedMessage();
		// String what = e.toString();
		// }

		while (cursor.moveToNext()) {

			Contact c = new Contact(
					cursor.getString(cursor
							.getColumnIndex(ContactsContract.Data.CONTACT_ID)),
					cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)),
					cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));

			contacts.add(c);

		}
	}

	/**
	 * Obtains the contact list for the currently selected account.
	 * 
	 * @return A cursor for for accessing the contact list.
	 */
	private Cursor getContacts() {
		Uri uri = ContactsContract.Data.CONTENT_URI;
		if (this.mSelectedAccount != null) {
			/*
			 * Filter by selected account
			 */
			uri = uri.buildUpon().appendQueryParameter(
					ContactsContract.RawContacts.ACCOUNT_NAME,
					mSelectedAccount.getName()).appendQueryParameter(
					ContactsContract.RawContacts.ACCOUNT_TYPE,
					mSelectedAccount.getType()).build();
		}

		String[] projection = new String[] { ContactsContract.Data._ID,
				ContactsContract.Data.CONTACT_ID,
				ContactsContract.Data.RAW_CONTACT_ID,
				ContactsContract.Data.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME };

		String where = ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereArgs = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

		String sortOrder = ContactsContract.Data.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		try {
			Cursor c = managedQuery(uri, projection, where, whereArgs,
					sortOrder);
			return c;
		}
		catch (Exception e) {
			String s = e.getMessage();
			Log.d(TAG, "getContacts() crashed: " + s);
		}

		return null;
	}

	private void fixNames() {
		for (Contact c : contacts) {
			Uri uri = ContactsContract.Data.CONTENT_URI;
			if (this.mSelectedAccount != null) {
				/*
				 * Filter by selected account
				 */
				uri = uri.buildUpon().appendQueryParameter(
						ContactsContract.RawContacts.ACCOUNT_NAME,
						mSelectedAccount.getName()).appendQueryParameter(
						ContactsContract.RawContacts.ACCOUNT_TYPE,
						mSelectedAccount.getType()).build();
			}

			String where = ContactsContract.Data.MIMETYPE
					+ " = ? AND "
					+ ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID
					+ " = ?";

			String[] whereArgs = new String[] {
					ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
					c.getId() };

			ContentValues values = new ContentValues();
			values
					.put(
							ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							c.getNewDisplayName(mFirstLast, mComma));

			try {
				getContentResolver().update(ContactsContract.Data.CONTENT_URI,
						values, where, whereArgs);
			}
			catch (Exception e) {
				String s = e.getMessage();
				Log.d(TAG, "fixNames() crashed: " + s);
			}
		}
	}

	/**
	 * Custom adapter used to display account icons and descriptions in the
	 * account spinner.
	 */
	private class AccountAdapter extends ArrayAdapter<AccountData> {
		public AccountAdapter(Context context,
				ArrayList<AccountData> accountData) {
			super(context, android.R.layout.simple_spinner_item, accountData);
			setDropDownViewResource(R.layout.account_entry);
		}

		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			// Inflate a view template
			if (convertView == null) {
				LayoutInflater layoutInflater = getLayoutInflater();
				convertView = layoutInflater.inflate(R.layout.account_entry,
						parent, false);
			}
			TextView firstAccountLine = (TextView) convertView
					.findViewById(R.id.firstAccountLine);
			TextView secondAccountLine = (TextView) convertView
					.findViewById(R.id.secondAccountLine);
			ImageView accountIcon = (ImageView) convertView
					.findViewById(R.id.accountIcon);

			// Populate template
			AccountData data = getItem(position);
			firstAccountLine.setText(data.getName());
			secondAccountLine.setText(data.getTypeLabel());
			Drawable icon = data.getIcon();
			if (icon == null) {
				icon = getResources().getDrawable(
						android.R.drawable.ic_menu_search);
			}
			accountIcon.setImageDrawable(icon);
			return convertView;
		}
	}

	/**
	 * A container class used to repreresent all known information about an
	 * account.
	 */
	private class AccountData {
		private String			mName;
		private String			mType;
		private CharSequence	mTypeLabel;
		private Drawable		mIcon;

		/**
		 * @param name
		 *            The name of the account. This is usually the user's email
		 *            address or username.
		 * @param description
		 *            The description for this account. This will be dictated by
		 *            the type of account returned, and can be obtained from the
		 *            system AccountManager.
		 */
		public AccountData(String name, AuthenticatorDescription description) {
			mName = name;
			if (description != null) {
				mType = description.type;

				// The type string is stored in a resource, so we need to
				// convert it into something
				// human readable.
				String packageName = description.packageName;
				PackageManager pm = getPackageManager();

				if (description.labelId != 0) {
					mTypeLabel = pm.getText(packageName, description.labelId,
							null);
					if (mTypeLabel == null) {
						throw new IllegalArgumentException(
								"LabelID provided, but label not found");
					}
				}
				else {
					mTypeLabel = "";
				}

				if (description.iconId != 0) {
					mIcon = pm.getDrawable(packageName, description.iconId,
							null);
					if (mIcon == null) {
						throw new IllegalArgumentException(
								"IconID provided, but drawable not " + "found");
					}
				}
				else {
					mIcon = getResources().getDrawable(
							android.R.drawable.sym_def_app_icon);
				}
			}
		}

		public String getName() {
			return mName;
		}

		public String getType() {
			return mType;
		}

		public CharSequence getTypeLabel() {
			return mTypeLabel;
		}

		public Drawable getIcon() {
			return mIcon;
		}

		public String toString() {
			return mName;
		}
	}

	@Override
	/**
	 * Updates account list spinner when the list of Accounts on the system changes. Satisfies
	 * OnAccountsUpdateListener implementation.
	 */
	public void onAccountsUpdated(Account[] a) {
		Log.i(TAG, "Account list update detected");
		// Clear out any old data to prevent duplicates
		mAccounts.clear();

		// Get account data from system
		AuthenticatorDescription[] accountTypes = AccountManager.get(this)
				.getAuthenticatorTypes();

		// Populate tables
		for (int i = 0; i < a.length; i++) {
			// The user may have multiple accounts with the same name, so we
			// need to construct a
			// meaningful display name for each.
			String systemAccountType = a[i].type;
			AuthenticatorDescription ad = getAuthenticatorDescription(
					systemAccountType, accountTypes);
			AccountData data = new AccountData(a[i].name, ad);
			mAccounts.add(data);
		}

		// Update the account spinner
		mAccountAdapter.notifyDataSetChanged();
	}

	/**
	 * Obtain the AuthenticatorDescription for a given account type.
	 * 
	 * @param type
	 *            The account type to locate.
	 * @param dictionary
	 *            An array of AuthenticatorDescriptions, as returned by
	 *            AccountManager.
	 * @return The description for the specified account type.
	 */
	private static AuthenticatorDescription getAuthenticatorDescription(
			String type, AuthenticatorDescription[] dictionary) {
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i].type.equals(type)) {
				return dictionary[i];
			}
		}
		// No match found
		throw new RuntimeException("Unable to find matching authenticator");
	}

	/**
	 * Update account selection. If NO_ACCOUNT is selected, then we prohibit
	 * inserting new contacts.
	 */
	private void updateAccountSelection() {
		// Read current account selection
		mSelectedAccount = (AccountData) mAccountSpinner.getSelectedItem();
	}
}