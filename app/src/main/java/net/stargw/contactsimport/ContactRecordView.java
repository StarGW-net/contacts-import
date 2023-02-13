package net.stargw.contactsimport;

// My own class for storing data.
// Submit cursor queries and then build my data from them
public class ContactRecordView {
	private String mText;
	private long mId;

	public ContactRecordView(String text, long id) {
		mText = text;
		mId = id;
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

	@Override
	public String toString() {
		return mText;
	}
}
