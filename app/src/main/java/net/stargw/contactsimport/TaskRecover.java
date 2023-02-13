package net.stargw.contactsimport;

import java.util.Iterator;

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
public class TaskRecover extends TaskCommonIn {

	TaskController myTaskController;
	TaskCallback callback;
	
	int max;
	int prog;
	Context context;
	
	Mappings mappingsRecover;
	PowerManager.WakeLock wakeLock;
	
	public TaskRecover(TaskController t,Context c, TaskCallback l)
	{
		context = c;
		myTaskController = t;
		callback = l;
		
		// mappingsRecover = m;
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	
	
	// so we can update periodically and dismiss it.
	Dialog dialog;
	
	protected void onPreExecute ()
	{
		max = Global.peopleRecover.size() - 1;
		
		Logs.myLog("Async Recover Started",1);
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
				Logs.myLog("Cancelled Recover",1);
				cancel(true);
			}
		});
		
		ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.progressBar);
		progress.setMax(max);
		
		TextView text1 = (TextView) dialog.findViewById(R.id.progressText1);
		text1.setText(String.format(context.getString(R.string.recovering01),max));
		
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
		int n = StartRecover();
		
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Recover operation took " + ((stop - start)/1000) + " seconds.",1);
		
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
		myTaskController.setTaskRecover(false);
		myTaskController.lastTask();
		
		// Display a popup which user has to acknowledge
		if (result == max)
		{
			Global.infoMessage2(context,
					context.getString(R.string.success),
					String.format(context.getString(R.string.recovered01),result,max,Global.accountName), 
					context.getString(R.string.viewContacts),1,null);
		} else {
			Global.infoMessage2(context,
					context.getString(R.string.failure),
					String.format(context.getString(R.string.recovered02),result,max,Global.accountName), // see logs for errors
					context.getString(R.string.viewContacts),1,null);
		}
		Global.peopleRecover = null;
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
	}
	
	protected void onCancelled() {
		wakeLock.release();
		dialog.dismiss();
		myTaskController.cancelTasks(); 
		
		Global.peopleRecover = null;
	
		Global.infoMessage2(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.recovered01),prog,max,Global.accountName), // see logs
				context.getString(R.string.viewContacts),1,null);
	
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
	public int StartRecover() { 
		
		CreateAccount(context);
		
		// map headings based on input file...which is in first row of recovery file.
		mappingsRecover = new Mappings();
		mappingsRecover.defaultPrefs();
		
		mappingsRecover.setConcatAddress(true);
		mappingsRecover.setIgnoreFirstLine(1);
		
		// populate mappings and then call generic add contacts
		
		Iterator<String[]> iterator = Global.peopleRecover.iterator();
		String[] contactRecord = iterator.next();
		
	
		// Construct the mappings based on the contents of the first field
		for (int i = 0; i < contactRecord.length; i++ )
		{
			// int n = contact[i]; // need a reverse name to int here!!
			for (int k = 0; k < Global.getContactFieldNum(); k++)
			{
				if (contactRecord[i].equals(Global.getContactField(k)))
				{
					mappingsRecover.setField(i,k);
					continue;
				}
			}
		}

		int num = AddContacts(context,Global.peopleRecover, mappingsRecover);
		
		Logs.myLog("Added " + num + " Contacts OK.",1);
		
		return num;

	}




	
}
