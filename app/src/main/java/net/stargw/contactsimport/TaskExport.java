package net.stargw.contactsimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.format.Time;

import androidx.core.content.FileProvider;


//
// Async task for Exporting contacts and communicating with the UI
//
class TaskExport extends AsyncTask<Void, String, Integer>
{
	int prog = 0;
	int max = 0;
	
	Mappings mappingsExport;
	Settings settings;
	
	String[] headerField = new String[Global.fields];
	
	Context context;
	
	int maxHeaders;
	
	TaskController myTaskController;
	PowerManager.WakeLock wakeLock;
	
	public TaskExport(TaskController t,Context c) // No call back required?
	{
		context = c;
		myTaskController = t;
		
		settings = new Settings();
		settings.load("export");
		
		mappingsExport = new Mappings();
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
	}
	

	
	// so we can update periodically and dismiss it.
	ProgressDialog dialog;
	
	protected void onPreExecute ()
	{

		// max = Global.CheckContacts(context); // use a local one
		max = Global.numContacts(settings.getAccountName(), settings.getAccountType(), false);
				
		Logs.myLog("Async Export Started",1);
		// Do I want to display anything here?
		// Maybe the popup warning - but can I cancel here?
		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(max);
		dialog.setMessage(String.format(context.getString(R.string.exporting01), Integer.toString(max)));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Logs.myLog("Cancelled Export",1);
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
		int n = startExport();
		// Timing
		time.setToNow();
		long stop = time.toMillis(false);
		Logs.myLog("Export operation took " + ((stop - start)/1000) + " seconds.",1);
		
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
		
		// Dismiss the progress dialog
		dialog.dismiss();
		
		myTaskController.setTaskExport(false);

		if (myTaskController.lastTask() == true)
		{
			// Display a popup which user has to acknowledge
			if (result == max)
			{
				// Share
				String iPath = settings.getCSVFile();

				// File f1 = context.getCacheDir();
				// Logs.myLog("CACHE = " + f1.toString(),2);

				File f = new File(iPath);
				// File f = new File(Global.getContext().getFilesDir(), "logfile.txt");
				Logs.myLog("READ PATH = " + f.toString(),2);

				// This provides a read only content:// for other apps
				Uri uri2 = FileProvider.getUriForFile(Global.getContext(),"eu.stargw.contactsimport.fileprovider",f);

				Logs.myLog("URI PATH = " + uri2.toString(),2);

				Intent intent2 = new Intent(Intent.ACTION_SEND);
				intent2.putExtra(Intent.EXTRA_STREAM, uri2);
				intent2.setType("text/csv");
				intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Global.getContext().startActivity(intent2);

			} else {
				Global.infoMessage2(context,
						context.getString(R.string.failure),
						String.format(context.getString(R.string.exported02),result,max,Global.accountName, settings.getCSVFile()),
						context.getString(R.string.share),2,settings.getCSVFile());
			}
		} else {
			// Do the next task
			myTaskController.nextTask();
		}
	}
	
	protected void onCancelled() {
		wakeLock.release();
		// Dismiss the progress dialog
		dialog.dismiss();
		Global.infoMessage(context,context.getString(R.string.cancelled),context.getString(R.string.exported03));
		myTaskController.cancelTasks(); 
	}
	
	
	public void displayProgress(int val, String buf){
		prog = val;
		publishProgress(buf);
	}

	private int startExport()
	{
		

		for (int i = 0; i < headerField.length; i++)
		{
			headerField[i] = "";
			
		}
		
		// Fix the first two
		headerField[0] = "vnd.android.cursor.item/name GIVEN_NAME";
		headerField[1] = "vnd.android.cursor.item/name FAMILY_NAME";
		
		mappingsExport.setIgnoreFirstLine(1);
		mappingsExport.setConcatAddress(true);
		settings.setCodePage(0); // will select this later...
		
		String exportLines[];
		exportLines = getContacts(); 
		
		int n = 0;
		if (exportLines != null)
		{

			Time time = new Time(Time.getCurrentTimezone());
			time.setToNow();
			String tstamp = time.format("%Y%m%d-%H%M%S");

			/*
			File newFolder = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.contactsFilesDir));
			if (!newFolder.exists()) {
				newFolder.mkdir();
			}
			*/

			// String iPath = context.getExternalCacheDir() + "/" + settings.getAccountName() + "-export-" + tstamp + ".csv";
			String iPath = context.getCacheDir() + "/" + settings.getAccountName() + "-export-" + tstamp + ".csv";
			File file = new File(iPath);
			settings.setCSVFile(file.getAbsolutePath());
			// mappingsExport.savePref();
			
			// Filename written before we call this func - to prefs file?
			n = writeCSVFile(exportLines);

		}
		return n;
	}


	

	protected int getMaxHeaders()
	{
		return mappingsExport.getFieldNum();
	}


	
	protected void parseData(String mimetype, String [] data, String [] exportFields)
	{

		String key;
		
		int type = 0;

		// if (mimetype.equals("vnd.android.cursor.item/name"))
		if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
		{

			
			// int  x = ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
			key = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + " " + "GIVEN_NAME";

			int a = getUnusedIndex(key, exportFields);
			if (a != -1)
			{
				exportFields[a] = data[2];
				// Logs.myLog("Save Given Name = " +  data[2],1);
			}
			
			key = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + " " + "FAMILY_NAME";
			a = getUnusedIndex(key, exportFields);
			if (a != -1)
			{
				exportFields[a] = data[3];
			}
			
			if ((data[5] != null) && (!(data[5].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + " " + "MIDDLE_NAME";
				a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[5];
				}
			}
			return;
		}

		if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type));
					if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
					{
						Logs.myLog("Custom: " + data[3] + " = " + data[1],2);
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}

				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}

		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.Email.getTypeLabelResource(type));
					if (type == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.Im.getTypeLabelResource(type));
					if (type == ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					// No get resource for this!
					if (type == ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					// No get resource for this!
					if (type == ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{

				key = ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE;
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}

		if (mimetype.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.Organization.getTypeLabelResource(type));
					if (type == ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.Event.getTypeResource(type)); // different
					if (type == ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				
				key = ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE;

				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					// exportFields[a] = data[1];
					exportFields[a] = getGroup(data[1]);
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				key = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
				try {
					type = Integer.parseInt(data[2]);
					key = key + " " + context.getString(ContactsContract.CommonDataKinds.StructuredPostal.getTypeLabelResource(type));
					if (type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM)
					{
						key = key + " " + data[3];
					}
				} catch (Exception e) {
					// No type
				}
				int a = getUnusedIndex(key, exportFields);
				if (a != -1)
				{
					exportFields[a] = data[1];

				}
			}
			return;
		}
		
		// Do I need to do all address fields as well?
		
		// Unknown!!
		if ((data[1] != null) && (!(data[1].isEmpty())))
		{
			key = mimetype;
			try {
				type = Integer.parseInt(data[2]);
				key = key + " " + context.getString(ContactsContract.CommonDataKinds.StructuredPostal.getTypeLabelResource(type));
				if (type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM)
				{
					key = key + " " + data[3];
				}
			} catch (Exception e) {
				// No type
			}
			int a = getUnusedIndex(key, exportFields);
			if (a != -1)
			{
				exportFields[a] = data[1];
			}
		}

	}

	protected String DumpContactsData(String rawID)
	{
		// long l = Long.valueOf(contactRecord.getID()).longValue();
		
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.appendQueryParameter(RawContacts._ID, rawID)
				.build();
	
		// String selection = ContactsContract.Data.DISPLAY_NAME + "=" + DatabaseUtils.sqlEscapeString(name);
		String selection = ContactsContract.Data.RAW_CONTACT_ID + "=" + rawID;
		
		String[] projection = new String[] {
				ContactsContract.Data._ID, 
				ContactsContract.Data.MIMETYPE,
				ContactsContract.Data.DATA1,
				ContactsContract.Data.DATA2,
				ContactsContract.Data.DATA3,
				ContactsContract.Data.DATA4,
				ContactsContract.Data.DATA5
		};
	
		String[] selectionArgs = null;
		
		// String sortOrder = ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		ContentResolver cr = context.getContentResolver();
		// Cursor cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, null);
		// Cursor cur = cr.query(uri, null, null, null, null);
		
		// String id = null;
		
		maxHeaders = getMaxHeaders();
		
		String exportFields[] = new String[mappingsExport.getFieldNum()];
		for (int i = 0; i< exportFields.length; i++)
		{
			exportFields[i] = ""; // blank them
		}
		
		if (cur.getCount() > 0) {
			Logs.myLog("Found matches: " + cur.getCount(),2); // matches is a row of data for that contact. e.g. phone number, email etc.

			while (cur.moveToNext()) {
				if (!cur.isNull(1)) // MIME Type
				{
					String mimetype = cur.getString(1);
					String data[] = new String[7];
					// 0 unused
					data[1] = cur.getString(2);
					data[2] = cur.getString(3);
					data[3] = cur.getString(4);
					data[4] = cur.getString(5);
					data[5] = cur.getString(6);
					parseData(mimetype, data, exportFields);
					// Logs.myLog("Data: " + mimetype + " , " + data1 + "," + data2 + "," + data3 ,2);
					// line = line + parseData(mimetype, data1, data2);
				}
			}
		}
		cur.close();
		
		//flatten exportFields();
		String line = "";
		for (int i = 0; i < maxHeaders; i++)
		{
			if ((exportFields[i] == null) || (exportFields[i].isEmpty()))
			{
				line = line + ",";
			} else {
				line = line + "\"" + exportFields[i].replace("\n","\r\n") + "\",";
			}

		}
		
		Logs.myLog("LINE = " + line,1);
		line = line.substring(0, line.length() - 1);

		
		return line;

	}
	

	
	protected String getGroup(String gid) {

		Cursor groupCursor = context.getContentResolver().query(
				ContactsContract.Groups.CONTENT_URI,
				new String[]{
					ContactsContract.Groups._ID,
					ContactsContract.Groups.TITLE
				}, ContactsContract.Groups._ID + "=" + gid, null, null
		);
		
		String title = null;
		if (groupCursor.getCount() > 0) {
			while (groupCursor.moveToNext()) {
				title = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.TITLE));
			}
		} else {
			Logs.myLog("No group found for id: " + gid,2);
		}
		
		groupCursor.close();
		
		Logs.myLog("Group = " + title,2);
		return title;
		
	}
	
	

	// Make generic with file name
	protected int writeCSVFile(String [] lines)
	{
		// is a global ?
		String fileName = settings.getCSVFile();
		
		File file = new File(fileName);
		
		FileOutputStream fos;
		
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
		} catch (Exception e) {
			Logs.myLog("Error creating export file: " + fileName,1);
			return -1;
		}
		

		
		//
		// Build the header line
		//
		String header = "";

		// String outlookHeaders[] = context.getResources().getStringArray(R.array.outlookEnglish);
		for (int i = 0; i< headerField.length; i++)
		{
			if (!(headerField[i].equals("")))
			{
				header = header + "\"" + headerField[i].replace("vnd.android.cursor.item/","").toUpperCase() + "\",";
			}
		}
		header = header.substring(0, header.length() - 1);
		header = header  + "\r\n";
		Logs.myLog("Header: " + header, 1);
		

		
		if (mappingsExport.getIgnoreFirstLine() == 1) // slightly odd logic because this var is reused
		{
			//
			// Write the header line
			//
			
			
			try {
				byte[] data = header.getBytes(settings.getCodePageText()); // charset!
				fos.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logs.myLog("Unable to write to file: " + e.toString(),1);
			}
		}

		
		int n = 0; // double!
		
		//
		// Write the contacts out line by line
		//
		for (int k = 0; k < lines.length; k++ )
		{
			// String displayName = (lines[0] + " " + lines[1]).trim();
			try {
				if (lines[k] != null)
				{
					// byte[] data = lines[k].getBytes();
					// byte[] data = getBytes(lines[k], getCodePage());
					byte[] data = lines[k].getBytes(settings.getCodePageText());
					fos.write(data);
					// displayProgress(n,"Writing to SD card.\n\nPlease wait...");
					// displayProgress(n,"Write:\n\n" + displayName);
					n++;
				}
				// displayProgress(k, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logs.myLog("Unable to write to file: " + e.toString(),1);
				// displayProgress(n,"Error:\n\n" + displayName);
				displayProgress(k,"Error writing contact: " + n);
			}
			
			// Check if cancelled
			if (isCancelled())
			{
				try {
					fos.flush();
					fos.close();
					Logs.myLog("CSV write file cancelled: " + fileName,1);
					return n;
				} catch (IOException e) {
					Logs.myLog("Unable to close file.",1);
				}
				file.delete();
				return 0; // do we let them cancel here?
			}
		}

		try {
			fos.flush();
			fos.close();
			Logs.myLog("CSV export file: " + fileName,1);

		} catch (IOException e) {
			Logs.myLog("Unable to close file.",1);
			return -1;
		}
		return n;
	}
	
	
	
	public String[] getContacts()
	{
		// reset headers

		ContentResolver cr = context.getContentResolver();
		
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.build();
	
		String selection = ContactsContract.Data.MIMETYPE + "=" + DatabaseUtils.sqlEscapeString(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		String[] projection = new String[] {
				ContactsContract.Data.RAW_CONTACT_ID, 
				ContactsContract.Data.DISPLAY_NAME,
		};
		
		Cursor cur = cr.query(uri, projection, selection, null, sortOrder);
		
		// Cursor cur = cr.query(rawContactUri,null, null, null, sortOrder);

		String exportLines[] = new String[Global.numContacts(settings.getAccountName(),settings.getAccountType(), false)]; // save upto x contacts
		
		int num = cur.getCount();
		int n = 0;
		if ( num > 0) {
			while (cur.moveToNext()) {
				String contact = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)); // Use this ID to get the name???
				Logs.myLog("Dump contact " + contact,2);
				exportLines[n] = DumpContactsData(id) + "\r\n";
				n++;
				// displayProgress(n,"Reading:\n\n" + contact);
				displayProgress(n,"");
				if (isCancelled())
				{
					return null; // do we let them cancel here?
				}
			}
		}
		
		cur.close();
		
		
		return exportLines;
	}

	
	protected int getUnusedIndex(String v, String [] exportFields)
	{
		
		for (int i = 0; i < headerField.length; i++) // use contacts not import...
		{
			if (headerField[i].equals(v))
			{
				if (exportFields[i].equals(""))
				{
					// Logs.myLog("Reused Field " + i + " = " + v,3);
					return i;
				}
			}
			
		}
			
		for (int j = 0; j < headerField.length; j++)
		{
			if (headerField[j].equals(""))
			{
				headerField[j] = v;  // make this a new column
				// Logs.myLog("New Field " + j + " = " + v,3);
				return j; 
			}
		}

		// Logs.myLog("No filed available!",3);
		return -1;
	}
	


	


}
