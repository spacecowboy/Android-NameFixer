package com.spacecowboy.contacts;

public class Contact {
	private String	id;

	public String getId() {
		return id;
	}

	private String	oldDisplayName	= "";

	public String getOldDisplayName() {
		return oldDisplayName;
	}

	private String	familyName	= "";

	public String getFamilyName() {
		return familyName;
	}

	private String	givenName	= "";

	public String getGivenName() {
		return givenName;
	}

	public Contact(String id, String givenname, String familyname) {
		this.id = id;
		// if (displayname != null)
		// this.oldDisplayName = displayname;
		if (familyname != null) this.familyName = familyname;
		if (givenname != null) this.givenName = givenname;
	}

	public String getNewDisplayName(boolean firstLast, boolean comma) {
		if (givenName != "" && familyName != "") {
			String displayName = "";
			/* Firstname Lastname */
			if (firstLast) {
				displayName = givenName + (comma ? ", " : " ") + familyName;
			} /* Lastname Firstname */
			else {
				displayName = familyName + (comma ? ", " : " ") + givenName;
			}
			/* Trim any excess spaces */
			displayName = displayName.trim();
			//if (displayName.endsWith(","))
			//	displayName = displayName
			//			.substring(0, displayName.length() - 1).trim();

			/* Finally return */
			return displayName;
		}
		else if (givenName != "") {
			return givenName;
		}
		else if (familyName != "") {
			return familyName;
		}
		else {
			throw (new IllegalArgumentException());
		}
	}

	/**
	 * Will only compare the IDs of the contacts. If the IDs are the same, they are considered equal.
	 */
	public boolean equals(Object o) {
		if (o.getClass().equals(this.getClass())) {
			// Right class atleast
			Contact c = (Contact) o;
			if (this.id.equals(c.getId())) return true;
		}
		return false;
	}
}
