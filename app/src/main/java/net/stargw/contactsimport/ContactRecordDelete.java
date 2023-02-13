package net.stargw.contactsimport;


// My own class for storing Groups and Contacts.
public class ContactRecordDelete {
	private long mId;
	private String mName;
	private String mText;
	private String mType;
	private boolean checked = false;
	
	public ContactRecordDelete(long id, String name, String text) {
		mId = id;
		mName = name;
		mText = text;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getmText() {
		return mText;
	}

	public void setmText(String mText) {
		this.mText = mText;
	}

	public String getmName() {
		return mName;
	}
	
	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmType() {
		return mType;
	}
	
	public void setmType(String mType) {
		this.mType = mType;
	}
	
	// What is this for??
	@Override
	public String toString() {
		return mText;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void toggleChecked() {
		checked = !checked;
	}
}

