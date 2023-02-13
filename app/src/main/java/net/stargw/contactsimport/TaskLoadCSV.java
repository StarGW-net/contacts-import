package net.stargw.contactsimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.stargw.contactsimport.utils.CSVReader;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;


//
// Delete all contacts. Nice little spinner. Cannot cancel this.
//
public class TaskLoadCSV extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	// String fileName;
	Uri fileName;
	String codePage;

	Boolean action;
	TaskCallback callback;
	
	TaskController myTaskController;
	
	Context context;

	
	// public TaskLoadCSV(TaskController t,Context c, String fn, String cp, TaskCallback l,  boolean a )
	TaskLoadCSV(TaskController t,Context c, Uri fn, String cp, TaskCallback l,  boolean a )
	{
		context = c;
		myTaskController = t;
		// fileName = fn;
		fileName = fn;
		codePage = cp;
		callback = l;
		action = a;
	}
	
	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute (){
		
		// max = Global.CheckContacts(context,Global.accountName,Global.accountType);
		/*
		File f = new File(fileName);
		int size = 0;
		try {
			FileInputStream is = new FileInputStream(f);
			size = is.available();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logs.myLog("File Error: " + e,1);
		}
		max = size / 1024;
		 */
		
		Logs.myLog("Async Load File Started",1);
		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(true);
		// dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// dialog.setMax(max);
		dialog.setMessage(context.getString(R.string.loading01));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false); // cannot cancel
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Logs.myLog("Cancel",1);
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
		
		// String responseBody = LoadContactsCSV(fileName, codePage);
		String responseBody = LoadContactsCSV4(fileName, codePage);

		// Errors are logged to logs
		
		if (responseBody == null)
		{
			if (action == false)
			{
				Global.peopleRecover  = null;
			} else {
				Global.peopleImport = null;
			}
			Logs.myLog("CSV file null;!",1);
			return 0;
		}
		
		// displayProgress(0,"");
		
		// Argh - how do I know which one :-(
		if (action == false)
		{
			Global.peopleRecover  = ParseContactsCSV(responseBody);
		} else {
			Global.peopleImport = ParseContactsCSV(responseBody);
		}

		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Load file operation took " + ((stop - start)/1000) + " seconds.",1);
		
		return 0;
	}

	protected void onCancelled() {
		// Dismiss the progress dialog
		dialog.dismiss();
		myTaskController.cancelTasks(); 

		Global.infoMessage(context,
				context.getString(R.string.cancelled),
				String.format(context.getString(R.string.loaded02),fileName) ); 
		if (action == false)
		{
			Global.peopleRecover  = null;
		} else {
			Global.peopleImport = null;
		}
		if (callback != null)
		{
			callback.onTaskDone(2);
		}
	}
	
	protected void onPostExecute(Integer result) {
		// int num2 = Global.checkContacts(Global.accountName,Global.accountType);
		
		// Dismiss the progress dialog
		dialog.dismiss();
		
		myTaskController.setTaskLoadCSV(false);

		if (myTaskController.lastTask() == true)
		{
			// Why no popup here?
			// Global.infoMessage(context,"Success!!","Deleted " + (max - num2) + " contacts from account \"" + Global.accountName + "\".\n\nSee logs for details.");
		} else {
			myTaskController.nextTask();
		}

		if (callback != null)
		{
			callback.onTaskDone(3);
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


	private String LoadContactsCSV4(Uri ContactsUrl, String codePage)
	{

		String text = null;;
		try {
			/*
			File f = new File(ContactsUrl);
			FileInputStream is = new FileInputStream(f);
			*/

			InputStream is = null;

			try {
				is = context.getContentResolver().openInputStream(fileName);
			} catch (Exception e) {
				Logs.myLog("Cannot open file! " + fileName,1);
				return null;
			}

			int size = is.available();
			Logs.myLog("File Size: " + size,1);

			byte[] buffer = new byte[size];

			is.read(buffer);
			is.close();
			// text = new String(buffer,"windows-1252");
			text = new String(buffer, codePage);
			// Save memory??
			buffer = null;
			Logs.myLog("Loading file using code page: " + codePage,1);
		} catch (IOException e) {
			Logs.myLog("File Error: " + e,1);
		}
		return text;
	}

	// Load intent into buffer...
	private String LoadContactsCSV3(Uri ContactsUrl, String codePage) {

		InputStream inputStream = null;

		try {
			inputStream = context.getContentResolver().openInputStream(fileName);
		} catch (Exception e) {
			Logs.myLog("Cannot open file! " + fileName,1);
			return null;
		}

		String text = null;
		StringBuffer sb = new StringBuffer();

		InputStreamReader isReader = new InputStreamReader(inputStream);
		//Creating a BufferedReader object
		BufferedReader reader = new BufferedReader(isReader);

		String str;
		try {
			while((str = reader.readLine())!= null){
				sb.append(str);
			}
		} catch (Exception e) {
			Logs.myLog("Error reading file file! " + fileName,1);
			return null;
		}

		// text = new String(sb.toString(), codePage);

		return sb.toString();

	}

	
	private String LoadContactsCSV2(String ContactsUrl, String codePage)  
	{

		String text = null;;
		try {
			File f = new File(ContactsUrl);
			FileInputStream is = new FileInputStream(f);
			int size = is.available();
			Logs.myLog("File Size: " + size,1);

			byte[] buffer = new byte[size];
/*
				int i;
				for (i = 0; i < (size - 1024); i = i + 1024)
				{
					is.read(buffer, i, 1024);
					displayProgress(((i+1024)/1024),"");
				}
				// Logs.myLog("Read: " + i + " remaining " + (size - i),1);
				if (i < size)
				{
					is.read(buffer, i, (size - i));
				}
*/
			is.read(buffer);
			is.close();
			// text = new String(buffer,"windows-1252");
			text = new String(buffer, codePage);
			// Save memory??
			buffer = null;
			Logs.myLog("Loading file using code page: " + codePage,1);
		} catch (IOException e) {
			Logs.myLog("File Error: " + e,1);
		}
		return text;
	}
	
	//
	// Parse contacts into my structure
	//
	// private List<ContactRecord> ParseContactsCSV(String responseBody) 
	private List<String[]> ParseContactsCSV(String responseBody) 
	{ 
		int num = 0;
		
		// new list
		List<String[]> people = new ArrayList<String[]>();
		
		try {	   
			CSVReader reader = new CSVReader(new StringReader(responseBody) );
			// save memory??
			responseBody = null;
			
			String[] line;
			
			while ((line = reader.readNext()) != null) {
				num++;
				// check line has some commas in!
				if (line.length > 2) 
				{
					people.add(line);
				} else {
					Logs.myLog("Disgarding line " + (num) + " with not enough commas in.",2);
				}
				// displayProgress(num,"x");
				if (isCancelled())
				{
					return null;
				}
				
				if (num > Global.maxContacts)
				{
					return people;
				}
			}
			// Save memory
			reader = null;
			Logs.myLog("Parsed " + (num) + " Contacts OK.",1);
			return people;
		}catch (Exception e) {
			Logs.myLog("Error parsing CSV: "+ e.toString(),1);
			return null;
		}
	}
	
	

}

