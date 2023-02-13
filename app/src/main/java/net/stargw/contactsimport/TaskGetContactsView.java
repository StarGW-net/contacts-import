package net.stargw.contactsimport;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.format.Time;


//
// Delete all contacts. Nice little spinner. Cannot cancel this.
//
public class TaskGetContactsView extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	boolean contacts= true;
	
	TaskController myTaskController;
	TaskCallback callback;
	
	int state;
	long groupID;
	
	Context context;
	
	Settings settings;
	
	List<ContactRecordView> contactsList;
	PowerManager.WakeLock wakeLock;
	
	public TaskGetContactsView(TaskController t,Context c, List<ContactRecordView> r, TaskCallback l, Settings s, int i, Long g)
	{
		context = c;
		myTaskController = t;
		contactsList = r;
		callback = l;
		settings = s;
		state = i;
		groupID = g;
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute (){
		

		max = Global.numContacts(settings.getAccountName(),settings.getAccountType(), false);
		
		dialog = new ProgressDialog(context);

		dialog.setMessage(context.getString(R.string.fetchingContacts));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false); // cannot cancel
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Logs.myLog("Cancelled Get Contacts",1);
				cancel(true);
			}
		});
		
		dialog.show();
	}

	protected Integer doInBackground(Void...arg0) 
	{
		Time time = new Time(Time.getCurrentTimezone());
		
		int n = 0;
		
		// Timing
		time.setToNow();
		long start = time.toMillis(false);
		Logs.myLog("Fetch Contacts/Groups Started",1);
		
		wakeLock.acquire(); 
		if (state == 2)
		{
			ListALLPeople(contactsList);
		} 
		
		if (state == 1)
		{
			ListPeople(contactsList, groupID);
		} 
		
		if (state == 0)
		{
			ListGroups(contactsList);
		}

		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Fetch operation took " + ((stop - start)/1000) + " seconds.",1);

		
		return n;
	}

	protected void onCancelled() 
	{
		wakeLock.release();
		dialog.dismiss();
		myTaskController.cancelTasks(); 
		
		// vHandler.sendMessage(Message.obtain(vHandler, 0, "Wibble")); // send message to let GUI to know to update.
		if (callback != null)
		{
			callback.onTaskDone(state);
		}
		
		Global.infoMessage(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.fetched01),prog,max,settings.getAccountName()) ); // see logs
	}
	
	protected void onPostExecute(Integer result) 
	{
		wakeLock.release();
		dialog.dismiss();
		myTaskController.setTaskGetContactsView(false);

		// vHandler.sendMessage(Message.obtain(vHandler, 0, "Wibble")); // send message to let GUI to know to update.
		
		if (callback != null)
		{
			callback.onTaskDone(state);
		}
		
		
		if (myTaskController.lastTask() == true)
		{
			// No popup
			/*
			if (max == prog)
			{
				Global.infoMessage(context,
						context.getString(R.string.success),
						String.format(context.getString(R.string.deleted01),prog,max,settings.getAccountName()) ); // see logs
			} else {
				Global.infoMessage(context,
						context.getString(R.string.failure),
						String.format(context.getString(R.string.deleted02),prog,max,settings.getAccountName()) ); // see logs
			}
			 */
			myTaskController.nextTask();
		}

	}
	
	protected void onProgressUpdate(String...message)
	{
	
		if (prog == 0)
		{
			dialog.setMessage(message[0]);
		} else {
			dialog.setProgress(prog);
		}
	}
	

	protected void displayProgress(int val, String buf){
		prog = val;
		publishProgress(buf);
	}
	

	
	//
	// List RAW Contacts
	//
	private void ListALLPeople(List<ContactRecordView> people)
	{

		people.clear();
		
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.build();
	
		String selection = ContactsContract.Data.MIMETYPE + "=" + DatabaseUtils.sqlEscapeString(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		
		String[] projection = new String[] {
				ContactsContract.Data.RAW_CONTACT_ID,
				ContactsContract.Data.CONTACT_ID, 
				ContactsContract.Data.DISPLAY_NAME,
		};
		
		String[] selectionArgs = null;
				
		ContentResolver cr = context.getContentResolver();

		Cursor cur = cr.query(uri, projection, selection, selectionArgs, null);
		
		if (cur.getCount() > 0) {
			Logs.myLog("Found matches: " + cur.getCount(),2); // matches is a row of data for that contact. e.g. phone number, email etc.

			while (cur.moveToNext()) {
				if (!cur.isNull(2))
				{
					people.add(new ContactRecordView(
							cur.getString(2),
							cur.getLong(1)));
				} else {
					people.add(new ContactRecordView(
							"<no display name>",
							cur.getLong(1)));
				}

					

			}
		}
		cur.close();

		// Sort rawContactsList because of multiple accounts
		Collections.sort(people, new Comparator<ContactRecordView>(){
			public int compare(ContactRecordView s1, ContactRecordView s2) {
			return s1.getmText().compareToIgnoreCase(s2.getmText());
			}
		});
		
	}
	


	
	//
	// Get people in a group - this gets the raw ID which is the Contact ID to use
	//
	private void ListPeople(List<ContactRecordView> people, Long group)
	{
		
		people.clear();
		// Note the different URI to GetALLPeople function
		// Uri uri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.build();

		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "="
			+ Long.toString(group) + " AND "
			+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
			+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

		String[] projection = new String[] {
				// ContactsContract.Data.RAW_CONTACT_ID,  // we do not want raw here because we pass to contacts app
				ContactsContract.Data.CONTACT_ID, // Aggregate _ID
				ContactsContract.Data.DISPLAY_NAME,
		};

		String[] selectionArgs = null;
		
		String sortOrder = ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				// Raw Contact ID is the one we want here
				// String id = cur.getString(cur.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)); 
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID)); // Aggregate _ID
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));	

				
				people.add(new ContactRecordView(
						name,
						Long.parseLong(id)));

				if (isCancelled())
				{
					cur.close();
					return;
				}
			}
		}
		cur.close();
		

	}

	//
	// Get a list of all the groups
	//
	private void ListGroups(List<ContactRecordView> groups)
	{
		groups.clear();
		// groups.add(new ContactRecordView("* ALL Contacts",0));
		
		// Cursor cur = cr.query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);
		
		// Uri uri = ContactsContract.Groups.CONTENT_URI; 
		Uri uri = ContactsContract.Groups.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(ContactsContract.Groups.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(ContactsContract.Groups.ACCOUNT_TYPE, settings.getAccountType())
				.build();
		
		String[] projection = new String[] {
				ContactsContract.Groups._ID,
				ContactsContract.Groups.TITLE
		};

		String selection = null;

		// This causes a crash. Why???
		// String selection = ContactsContract.Groups.ACCOUNT_NAME + "=pcsc";

		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				// Raw Contact ID is the one we want here
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Groups._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Groups.TITLE));	
				long idl = cur.getLong(cur.getColumnIndex(ContactsContract.Groups._ID));
				
				groups.add(new ContactRecordView(
						name + " [" + numInGroup(idl) + "]" ,
						Long.parseLong(id)));
				if (isCancelled())
				{
					cur.close();
					return;
				}
			}
		}
		cur.close();

		
	}
	
	
	private String numInGroup(long group)
	{
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.build();

		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "="
			+ Long.toString(group) + " AND "
			+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
			+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

		String[] projection = new String[] {
				// ContactsContract.Data.RAW_CONTACT_ID,  // we do not want raw here because we pass to contacts app
				ContactsContract.Data.CONTACT_ID, // Aggregate _ID
				ContactsContract.Data.DISPLAY_NAME,
		};

		String[] selectionArgs = null;
		
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		
		int i = cur.getCount();
		cur.close();
		
		return String.valueOf(i);
				
	}
}

