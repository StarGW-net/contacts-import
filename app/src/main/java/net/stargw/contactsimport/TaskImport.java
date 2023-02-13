package net.stargw.contactsimport;

import android.app.Dialog;
import android.content.Context;
import android.os.PowerManager;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

//
// Async task for importing contacts and communicating with the UI
//
public class TaskImport extends TaskCommonIn {

	TaskController myTaskController;
	
	int max;
	int prog;
	boolean visible = true;
	TaskCallback callback;
	
	Context context;
	
	Mappings mappingsImport;
	// Settings settings;
	
	PowerManager.WakeLock wakeLock;

	
	public TaskImport(TaskController t,Context c, TaskCallback l)
	{
		context = c;
		myTaskController = t;
		callback = l;
		
		mappingsImport = new Mappings();
		mappingsImport.load();
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	
	
	// so we can update periodically and dismiss it.
	Dialog dialog;
	
	protected void onPreExecute ()
	{
		// Global.notification(context,notify,"Import Contacts");
		
		max = Global.peopleImport.size() - mappingsImport.getIgnoreFirstLine();
		
		Logs.myLog("Async Import Started",1);

		dialog = new Dialog(context);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_progress);
		
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);

		
		Button dialogButton = (Button) dialog.findViewById(R.id.progressButton);

		dialogButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				dialog.dismiss();
				Logs.myLog("Cancelled Import",1);
				cancel(true);
			}
		});
		
		ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.progressBar);
		progress.setMax(max);
		
		TextView text1 = (TextView) dialog.findViewById(R.id.progressText1);
		
		// Adding x contacts
		text1.setText(String.format(context.getString(R.string.adding01),max));
		
		TextView text2 = (TextView) dialog.findViewById(R.id.progressText2);
		text2.setText(context.getString(R.string.pleaseWait));
		
		dialog.show();
	}
	
	protected Integer doInBackground(Void...arg0) 
	{
		Time time = new Time(Time.getCurrentTimezone());
		
		// Timing
		time.setToNow();
		long start = time.toMillis(false);
		

		
		wakeLock.acquire(); 
		int n = StartImport();

		
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Import operation took " + ((stop - start)/1000) + " seconds.",1);
		
		return n;
	}

	protected void onProgressUpdate(String...message)
	{
	
		if (message[0].isEmpty() == false)
		{
			TextView text2 = (TextView) dialog.findViewById(R.id.progressText2);
			text2.setText(message[0]);
		}

		ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.progressBar);
		progress.setProgress(prog);
		
		TextView text3 = (TextView) dialog.findViewById(R.id.progressPercent);
		float p1 = prog;
		float p2 = max;
		float p = p1/p2 * 100;
		int x = (int) p;
		
		// Logs.myLog("Percent = " + p,1);
		text3.setText(x + "%");
		
		TextView text4 = (TextView) dialog.findViewById(R.id.progressCount);
		text4.setText(prog + "/" + max);
		
	}
	

	protected void onPostExecute(Integer result) 
	{

		wakeLock.release();
		dialog.dismiss();
		myTaskController.setTaskImport(false);
		// myTaskController.nextTask();
		myTaskController.lastTask();

		Global.peopleImport = null;
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
		
		// Display a popup which user has to acknowledge
		if (result == max)
		{
			Global.infoMessage2(context,
					context.getString(R.string.success),
					String.format(context.getString(R.string.added01),result,max,Global.accountName), // see logs
					context.getString(R.string.viewContacts),1,null);
		} else {
			Global.infoMessage2(context,
					context.getString(R.string.failure),
					String.format(context.getString(R.string.added02),result,max,Global.accountName), // see logs for errors
					context.getString(R.string.viewContacts),1,null);
		}
	}
	
	protected void onCancelled() {
		// Dismiss the progress dialog
		wakeLock.release();
		dialog.dismiss();
		myTaskController.cancelTasks(); 
		// notify.cancel(666);

		
		Global.infoMessage2(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.added01),prog,max,Global.accountName), // see logs
				context.getString(R.string.viewContacts),1,null);

		Global.peopleImport = null;
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
	}
	
	@Override
	protected void displayProgress(int val, String buf){
		prog = val;
		publishProgress(buf);
	}

	
	//
	public int StartImport() { 
		
		CreateAccount(context);
		
		int num = AddContacts(context, Global.peopleImport, mappingsImport);
		
		Logs.myLog("Added " + num + " Contacts OK.",1);
		
		return num;

	}



}
