package net.stargw.contactsimport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.format.Time;


//
// Delete all contacts. Nice little spinner. Cannot cancel this.
//
public class TaskDeleteAll extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	
	TaskController myTaskController;
	TaskCallback callback;
	
	Context context;
	PowerManager.WakeLock wakeLock;
	
	public TaskDeleteAll(TaskController t,Context c, TaskCallback l)
	{
		context = c;
		myTaskController = t;
		callback = l;
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	
	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute (){
		
		max = Global.numContacts(Global.accountName,Global.accountType, false);
		
		Logs.myLog("Async Delete Started",1);
		dialog = new ProgressDialog(context);
		// dialog.setIndeterminate(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(max);
		dialog.setMessage(String.format(context.getString(R.string.deleting01),max));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false); // cannot cancel
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancelDelete), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Logs.myLog("Cancelled delete",1);
				cancel(true);
			}
		});
		
		dialog.show();
	}

	protected Integer doInBackground(Void...arg0) 
	{
		Time time = new Time(Time.getCurrentTimezone());
		
		// Timing
		time.setToNow();
		long start = time.toMillis(false);
		
		wakeLock.acquire(); 
		
		// Delete ALL contacts - not yet
		// DelContacts(); 
		DelContactsOneByOne();
		
		// Delete ALL groups -not yet
		DelGroups();
		
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Delete operation took " + ((stop - start)/1000) + " seconds.",1);
		
		return 0;
	}

	protected void onCancelled() {
		wakeLock.release();
		// Dismiss the progress dialog
		dialog.dismiss();
		myTaskController.cancelTasks(); 
		
		Global.infoMessage(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.deleted01),prog,max,Global.accountName) );
		
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
	}
	
	protected void onPostExecute(Integer result) {
		int num2 = Global.numContacts(Global.accountName,Global.accountType, false);
		
		wakeLock.release();
		// Dismiss the progress dialog
		dialog.dismiss();
		
		myTaskController.setTaskDelete(false);
		
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
		
		if (myTaskController.lastTask() == true)
		{
			if (num2 == 0)
			{
				Global.infoMessage(context,
						context.getString(R.string.success),
						String.format(context.getString(R.string.deleted01),prog,max,Global.accountName) ); // see logs
			} else {
				Global.infoMessage(context,
						context.getString(R.string.failure),
						String.format(context.getString(R.string.deleted02),prog,max,Global.accountName) ); // see logs
			}
		} else {
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
	// Delete ALL groups from specified account
	//
	public boolean DelGroups() {

		boolean state;
		
		Uri ContactGroupUri = ContactsContract.Groups.CONTENT_URI;

		Uri DeleteUri = ContactGroupUri.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, Global.accountName)
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, Global.accountType)
		.build();


		
		try {
			context.getContentResolver().delete(DeleteUri,null,null);
			Logs.myLog("Deleted all groups ok.",1);
			state = true;
		} catch (Exception e) {
			Logs.myLog("Error deleting all groups - " + e.toString(),1);
			state = false;
		}
		
		return state;

	}
	
	
	
	//
	// Delete ALL raw contacts in an Account
	//
	public int DelContacts() 
	{

		int state = 0;
		
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, Global.accountName)
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, Global.accountType)
		.build();

		// Faster but cannot cancel
		try {
			context.getContentResolver().delete(rawContactUri,null,null);
			Logs.myLog("Delete all contacts ok.",1);
			state = 1;
		} catch (Exception e) {
			Logs.myLog("Error deleting all contacts - " + e.toString(),1);
			state = 0;
		}

		
		return state; // not really used

	}
	
	
	//
	// Delete ALL raw contacts in an Account one by one. Allows cancel but slow
	//
	private int DelContactsOneByOne() 
	{

		
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, Global.accountName)
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, Global.accountType)
		.build();

		String[] projection = new String[] {
				RawContacts._ID, 
		};
		
		Cursor cur = context.getContentResolver().query(rawContactUri,projection,null,null,null);
		int num = cur.getCount();
		
		Logs.myLog("Number of contacts to delete: " + num,1);
		
		int n = 0;
		if ( num > 0) {
			while (cur.moveToNext()) {
				n++;
				// String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String id = cur.getString(0);
				
				Uri ContactRawUri = Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, id);
								
				Uri DeleteUri = ContactRawUri.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.build();
				
				try {
					context.getContentResolver().delete(DeleteUri,null,null);
					Logs.myLog("Delete id: " + id,2);
					// myTaskExport.displayProgress(0,"Deleted:\n\n" + contact);
				} catch (Exception e) {
					Logs.myLog("Error deleting id: " + id + " " + e.toString(),1);
					// displayProgress(0,"Delete Error. Contact id:\n\n" + id);
				}
				displayProgress(n,"");
				if (isCancelled())
				{
					return 0;
				}
			}
		}
		
		
		return n; // not really used

	}
	
	private int DeleteChunks()
	{

			
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, Global.accountName)
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, Global.accountType)
		// .appendQueryParameter(ContactsContract.Data.DISPLAY_NAME,"Steve Watts")
		.build();


		/*
		 *  String[] selectionArgs=new String[]{String.valueOf(1)}
		 // this is for which argument to match   with TYPE=1 and delete row
		 String selection=""+TablenName.TYPE+"=?"; //  this is where condition 
		 getContentResolver().delete( Call.CONTENT_URI, selection, selectionArgs);
		 */
		// Faster but cannot cancel
		
		String[] selectionArgs=new String[]{"Steve Watts"};
		String selection=""+ContactsContract.Data.DISPLAY_NAME+"=?";
		
		try {
			context.getContentResolver().delete(rawContactUri,selection,selectionArgs);
			Logs.myLog("Delete all contacts ok.",1);
		} catch (Exception e) {
			Logs.myLog("Error deleting all contacts - " + e.toString(),1);

		}
		
		return 0;
	}

}

