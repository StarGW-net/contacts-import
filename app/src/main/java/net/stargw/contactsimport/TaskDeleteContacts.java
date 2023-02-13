package net.stargw.contactsimport;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


//
// Delete all contacts. Nice little spinner. Cannot cancel this.
//
public class TaskDeleteContacts extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	
	TaskCallback callback;
	TaskController myTaskController;
	
	Context context;
	
	Settings settings;
	
	ArrayList<ContactRecordDelete> rawContactsList;
	PowerManager.WakeLock wakeLock;
	
	public TaskDeleteContacts(TaskController t,Context c, ArrayList<ContactRecordDelete> r, TaskCallback l, int b)
	{
		context = c;
		myTaskController = t;
		rawContactsList = r;
		// vHandler = h;
		max = b;

		callback = l;
		
		settings = new Settings();
		settings.load("del");
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute (){
		

		// max = Global.CheckContacts(context,Global.accountName,Global.accountType);
		

		dialog = new ProgressDialog(context);

		Logs.myLog("Async Delete Started",1);
		dialog.setMessage(String.format(context.getString(R.string.deleting01),max));
		
		// dialog.setIndeterminate(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(max);
		
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false); 
		
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
		
		int n = 0;
		
		// Timing
		time.setToNow();
		long start = time.toMillis(false);
		
		wakeLock.acquire(); 

		n = DelContacts();
		if ( (settings.getAccountName().equals(Global.accountName)) && (max == Global.numContacts(Global.accountName,Global.accountType, false)) )
		{
			DelGroups();
		}


		
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Delete operation took " + ((stop - start)/1000) + " seconds.",1);

		
		return n;
	}

	protected void onCancelled() {
		wakeLock.release();
		
		// Dismiss the progress dialog
		dialog.dismiss();
		myTaskController.cancelTasks(); 
		
		myInfoMessage(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.deleted01),prog,max,settings.getAccountName()) ); // see logs
	}
	
	protected void onPostExecute(Integer result) {
		wakeLock.release();
		
		// Dismiss the progress dialog
		dialog.dismiss();
		
		myTaskController.setTaskDeleteContacts(false);
		
		// Another task will update display
		// vHandler.sendMessage(Message.obtain(vHandler, 0, "Wibble")); // send message to let GUI to know to update.
		
		myTaskController.lastTask();
		// we always want to display a message here even if not last task
		if (max == prog)
		{
			myInfoMessage(context,
					context.getString(R.string.success),
					String.format(context.getString(R.string.deleted01),prog,max,settings.getAccountName()) ); // see logs
		} else {
			myInfoMessage(context,
					context.getString(R.string.failure),
					String.format(context.getString(R.string.deleted02),prog,max,settings.getAccountName()) ); // see logs
		}
		// Display a popup which user has to acknowledge - compare prog to max??


		myTaskController.nextTask();


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
	// Delete a selected contacts
	//
	private int DelContacts() {

		int n = 0;
		Iterator<ContactRecordDelete> it = rawContactsList.iterator();
		while(it.hasNext())
		{
			ContactRecordDelete obj = it.next();
			//Do something with obj
			if (obj.isChecked())
			{

				Long id = obj.getId();
				
				Uri ContactRawUri = Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, Long.toString(id));
				
				
				Uri DeleteUri = ContactRawUri.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.build();
				
				try {
					context.getContentResolver().delete(DeleteUri,null,null);
				} catch (Exception e) {
					// status = "ERROR DELETING: " + id + " " + name;
				}
				n++;
				displayProgress(n,"");
				if (isCancelled())
				{
					return n;
				}
		    }
		}
		return n;

	}
	
	//
	// Delete ALL groups from specified account
	//
	public boolean DelGroups() {

		boolean state;
		
		Uri ContactGroupUri = ContactsContract.Groups.CONTENT_URI;

		Uri DeleteUri = ContactGroupUri.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
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
	// Display a popup info screen
	//
	public void myInfoMessage(final Context context, String header, String message)
	{
		final Dialog info = new Dialog(context);

		info.setContentView(R.layout.dialog_info);
		info.setTitle(header);
		
		TextView text = (TextView) info.findViewById(R.id.infoMessage);
		text.setText(message);
		text.setGravity(Gravity.CENTER_HORIZONTAL);  
		
		Button dialogButton = (Button) info.findViewById(R.id.infoButton);

		
		dialogButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				// notificationCancel(context);
				info.cancel();
				TaskController myTaskController = new TaskController();
				myTaskController.setTaskGetContactsDelete(true);
				myTaskController.myTaskGetContactsDelete = new TaskGetContactsDelete(myTaskController,context, rawContactsList, callback, settings, true);
				myTaskController.nextTask();
			}
		});
		

		info.show();
		Logs.myLog(header + ":" + message,1);
	}
}

