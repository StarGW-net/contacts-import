package net.stargw.contactsimport;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.PowerManager;
import android.text.format.Time;


//
// Async task for Exporting contacts and communicating with the UI
//
class TaskBackup extends TaskCommonOut
{
	int prog = 0;
	int max = 0;
	TaskCallback callback;
	
	TaskController myTaskController;
	PowerManager.WakeLock wakeLock;
	
	// Settings settings;
	
	boolean doExport;
	int codePage;
	
	public TaskBackup(TaskController t,Context c, int cp, boolean e, TaskCallback l)
	{
		context = c;
		myTaskController = t;
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
		
		settings = new Settings();
		// settings.load("export");
		callback = l;
		
		doExport = e;
		codePage = cp;
	}
	

	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute ()
	{

		max = Global.numContacts(Global.accountName, Global.accountType, false);
				
		Logs.myLog("Async Backup Started",1);

		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(max);
		if (doExport == false)
		{
			dialog.setMessage(String.format(context.getString(R.string.backingUp01),max));
		} else {
			dialog.setMessage(String.format(context.getString(R.string.exporting01),max));
		}
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Logs.myLog("Cancelled Backed up",1);
				cancel(true);
			}
		});
		
		dialog.show();
	}
	
	@Override
	protected Integer doInBackground(Void...arg0) 
	{
		Time time = new Time(Time.getCurrentTimezone());
		
		// Timing
		time.setToNow();
		long start = time.toMillis(false);
		
		wakeLock.acquire(); 
		int n = startBackup();
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Back up operation took " + ((stop - start)/1000) + " seconds.",1);
		
		return n;
	}

	protected void onProgressUpdate(String...message)
	{
	
		String s = message[0];
		if (s.isEmpty() == false)
		{
			dialog.setMessage(s);
		}
		dialog.setProgress(prog);

	}

	protected void onPostExecute(Integer result) 
	{
		wakeLock.release();
		dialog.dismiss();
		myTaskController.setTaskBackup(false);

		// too dangerous!
		// ((TaskCallback)context).onTaskDone(1);
		
		// Maybe abort?
		if (callback != null)
		{
			callback.onTaskDone(1);
		}

		
		if (result != max)
		{
			if (doExport == false)
			{
				Global.infoMessage(context,
						context.getString(R.string.failure),
						String.format(context.getString(R.string.backedUp02),result,max,Global.accountName) );
				// Delete the exported file
				// File file = new File(mappingsGeneric.getCSVFile());
				// file.delete();
			} else {
				Global.infoMessage2(context,
						context.getString(R.string.failure),
						String.format(context.getString(R.string.exported02),result,max,Global.accountName, settings.getCSVFile()),
						context.getString(R.string.share),2,settings.getCSVFile());
			}
		} else {
			if (myTaskController.lastTask() == true)
			{
				if (doExport == false)
				{
					Global.infoMessage(context,
							context.getString(R.string.success),
							String.format(context.getString(R.string.backedUp01),result,max,Global.accountName) );
					tidyBackupFiles();
				} else {
					Global.infoMessage2(context,
							context.getString(R.string.success),
							String.format(context.getString(R.string.exported01),result,max,Global.accountName, settings.getCSVFile()),
							context.getString(R.string.share),2,settings.getCSVFile());
				}
			} else {
				// Do the next task
				myTaskController.nextTask();
			}
		}
	}
	
	protected void onCancelled() {
		wakeLock.release();
		dialog.dismiss();
		myTaskController.cancelTasks(); 

		if (doExport == false)
		{
			Global.infoMessage(context,context.getString(R.string.cancelled),context.getString(R.string.backedUp03));
		} else {
			Global.infoMessage(context,context.getString(R.string.cancelled),context.getString(R.string.exported03));
		}
	}
	
	
	@Override
	public void displayProgress(int val, String buf){
		prog = val;
		publishProgress(buf);
	}

	private int startBackup()
	{
		mappingsGeneric = new Mappings();
		
		mappingsGeneric.setField(0,Global.GIVEN_NAME);
		mappingsGeneric.setField(1,Global.FAMILY_NAME);
		
		for (int i = 2; i < mappingsGeneric.getFieldNum(); i++)
		{
			mappingsGeneric.setField(i,-1);
			
		}
		mappingsGeneric.setIgnoreFirstLine(1);
		mappingsGeneric.setConcatAddress(true);
		settings.setAccountName(Global.accountName);
		settings.setAccountType(Global.accountType);
		
		settings.setCodePage(codePage);
		
		String exportLines[];
		exportLines = getContacts(); 
		
		int n = 0;
		if (exportLines != null)
		{

			Time time = new Time(Time.getCurrentTimezone());
			time.setToNow();
			String tstamp = time.format("%Y%m%d-%H%M%S");
			
			File file;
			if (doExport == false)
			{
				file = new File(context.getFilesDir(), "backup-" + tstamp + ".bak");
				settings.setCSVFile(file.getAbsolutePath());
				// mappingsGeneric.savePref();
			} else {
				File newFolder = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.contactsFilesDir));
				if (!newFolder.exists()) {
					newFolder.mkdir();
				}
				
				file = new File(newFolder, settings.getAccountName() + "-export-" + tstamp + ".csv");
				settings.setCSVFile(file.getAbsolutePath());
			}
			
			// Filename written before we call this func - to prefs file?
			n = writeCSVFile(exportLines);
		}
		return n;
	}

	//
	// Have as a default meho - but Override in Export. Same code so keep in common 
	// But have stubs that override and ref it through another func
	//
	@Override
	protected int getUnusedIndex(int v, String [] exportFields)
	{
		return getUnusedIndexMax(v, exportFields);
	}
	
	@Override
	protected int getMaxHeaders()
	{
		return mappingsGeneric.getFieldNum();
	}

	private void tidyBackupFiles()
	{
		File file = new File(settings.getCSVFile());
		File dir = file.getParentFile();

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				//File sel = new File(dir, filename);
				return filename.toLowerCase().contains(".bak") && (!filename.startsWith("."));
			}
		};
		
		String[] mFileList = dir.list(filter);
		
		Arrays.sort(mFileList);
		
		Logs.myLog("Number of Backup Files = " + mFileList.length,1);
		
		if ( mFileList.length > 10)
		{
			// for (int i = (mFileList.length - 1); i > 4; i--)
			// for (int i = 5 ; i < mFileList.length; i++)
			for (int i = 0; i < (mFileList.length - 10); i++ )
			{
				Logs.myLog("Delete Backup File = " + mFileList[i],1);
				File dfile = new File(dir,mFileList[i]);
				dfile.delete();
			}
		}
}



}
