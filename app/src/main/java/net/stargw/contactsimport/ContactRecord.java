package net.stargw.contactsimport;

public class ContactRecord {

	public ContactRecord() {
		// TODO Auto-generated constructor stub
	}

	private String mText[];
	private String id;
	private Boolean exists;

	public ContactRecord(String text[]) {
		mText = text; // holds an array!
	}
	
	public String[] getmText() {
		return mText;
	}

	public void setmText(String[] mText) {
		this.mText = mText;
	}
	
	public void setID(String i) {
		this.id = i;
	}
	
	public String getID() {
		return id;
	}
	
	public void setExists() {
		this.exists = true;
	}
	
	public Boolean getExists()
	{
		return exists;
	}
		
 // surely this is just a simple array!
}
