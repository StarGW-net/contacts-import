package net.stargw.contactsimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class TaskGetContactsDelete extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	boolean contacts= true;
	
	TaskController myTaskController;
	TaskCallback callback;
	
	Context context;
	
	Settings settings;
	
	ArrayList<ContactRecordDelete> contactsList;
	PowerManager.WakeLock wakeLock;
	
	public TaskGetContactsDelete(TaskController t,Context c, ArrayList<ContactRecordDelete> r, TaskCallback l, Settings s, Boolean b)
	{
		context = c;
		myTaskController = t;
		contactsList = r;
		callback = l;
		settings = s;
		contacts = b;
		
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
		if (contacts == true)
		{
			ListRawContacts5(contactsList);
		} else {
			ListGroups2(contactsList);
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
			callback.onTaskDone(0);
		}
		
		Global.infoMessage(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.fetched01),prog,max,settings.getAccountName()) ); // see logs
	}
	
	protected void onPostExecute(Integer result) 
	{
		wakeLock.release();
		dialog.dismiss();
		myTaskController.setTaskGetContactsDelete(false);

		// vHandler.sendMessage(Message.obtain(vHandler, 0, "Wibble")); // send message to let GUI to know to update.
		
		if (callback != null)
		{
			callback.onTaskDone(1);
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
	private void ListRawContacts5(ArrayList<ContactRecordDelete> rawContactsList)
	{

		rawContactsList.clear();
		
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
			// Logs.myLog("Found matches: " + cur.getCount(),1); // matches is a row of data for that contact. e.g. phone number, email etc.

			while (cur.moveToNext()) {
				if (!cur.isNull(2))
				{
					rawContactsList.add(new ContactRecordDelete(
							cur.getLong(0),
							cur.getString(2),
							cur.getString(2)));
				} else {
					rawContactsList.add(new ContactRecordDelete(
							cur.getLong(1),
							"<no display name>",
							"<no display name>"));
				}

					

			}
		}
		cur.close();

		// Sort rawContactsList because of multiple accounts
		Collections.sort(rawContactsList, new Comparator<ContactRecordDelete>(){
			public int compare(ContactRecordDelete s1, ContactRecordDelete s2) {
			return s1.getmName().compareToIgnoreCase(s2.getmName());
			}
		});
		
	}
	
	
	//
	// List all Groups - show account owners
	//
	private void ListGroups2(ArrayList<ContactRecordDelete> groupList) {

		groupList.clear();
		
		String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";
		

		Uri ContactGroupUri = ContactsContract.Groups.CONTENT_URI;

		Uri AddGroupUri = ContactGroupUri.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(ContactsContract.Groups.ACCOUNT_NAME, settings.getAccountName())
		.appendQueryParameter(ContactsContract.Groups.ACCOUNT_TYPE, settings.getAccountType())
		.build();

		ContentResolver cr = context.getContentResolver();
		
		// Get ALL contacts from a specific account
		Cursor cur = cr.query(AddGroupUri,null, null, null, sortOrder);
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				String id = cur.getString(cur.getColumnIndex(ContactsContract.Groups._ID));
				String source_id = cur.getString(cur.getColumnIndex(ContactsContract.Groups.SOURCE_ID));	
				String title = cur.getString(cur.getColumnIndex(ContactsContract.Groups.TITLE));
				if (title != null)
				{
					// String text = "ID = " + id + "\n" + "SOURCE_ID = " + source_id + "\n" + "TITLE = " + title;

					// String text = title + " (" + id + ")";
					String text = title;
					groupList.add(new ContactRecordDelete(
							Long.parseLong(id),
							title,
							text));	
				}
				if (isCancelled())
				{
					cur.close();
					return;
				}
			}
			cur.close();
		}

		
		Collections.sort(groupList, new Comparator<ContactRecordDelete>(){
			public int compare(ContactRecordDelete s1, ContactRecordDelete s2) {
				return s1.getmName().compareToIgnoreCase(s2.getmName());
				}
		});
	}
	
}

