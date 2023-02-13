package net.stargw.contactsimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

public class TaskCommonOut extends AsyncTask<Void, String, Integer>{

	public TaskCommonOut() {
		// TODO Auto-generated constructor stub
	}

	Mappings mappingsGeneric;
	Settings settings;
	
	Context context;
	
	int maxHeaders;
	
	@Override
	protected Integer doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	
	// Will be overridden
	protected int getUnusedIndex(int i, String[] a)
	{
		return 0;
	}
	
	
	protected void parseData(String mimetype, String [] data, String [] exportFields)
	{

		
		// if (mimetype.equals("vnd.android.cursor.item/name"))
		if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
		{
			int a = getUnusedIndex(Global.GIVEN_NAME, exportFields);
			if (a != -1)
			{
				exportFields[a] = data[2];
				// Logs.myLog("Save Given Name = " +  data[2],1);
			}
			a = getUnusedIndex(Global.FAMILY_NAME, exportFields);
			if (a != -1)
			{
				exportFields[a] = data[3];
			}
			if ((data[5] != null) && (!(data[5].isEmpty())))
			{
				a = getUnusedIndex(Global.MIDDLE_NAME, exportFields);
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
				int type = Integer.parseInt(data[2]); 
					
				if (type == Phone.TYPE_HOME)
				{
					int a = getUnusedIndex(Global.PHONE_HOME, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_WORK)
				{
					int a = getUnusedIndex(Global.PHONE_WORK, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_OTHER)
				{
					int a = getUnusedIndex(Global.PHONE_OTHER, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_MOBILE)
				{
					int a = getUnusedIndex(Global.MOBILE, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_FAX_WORK)
				{
					int a = getUnusedIndex(Global.FAX, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_WORK_MOBILE)
				{
					int a = getUnusedIndex(Global.MOBILE_WORK, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Phone.TYPE_CUSTOM)
				{
					if (data[3].equals(Global.getContactField(Global.MOBILE_WORK)))
					{	
						int a = getUnusedIndex(Global.MOBILE_WORK, exportFields);
						if (a != -1)
						{
							exportFields[a] = data[1];
						}
					}
					if (data[3].equals(Global.getContactField(Global.MOBILE_PERSONAL)))
					{	
						int a = getUnusedIndex(Global.MOBILE_PERSONAL, exportFields);
						if (a != -1)
						{
							exportFields[a] = data[1];
						}
					}
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int type = Integer.parseInt(data[2]); 
					
				if (type == Email.TYPE_HOME)
				{
					int a = getUnusedIndex(Global.EMAIL_HOME, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Email.TYPE_WORK)
				{
					int a = getUnusedIndex(Global.EMAIL_WORK, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
				if (type == Email.TYPE_OTHER)
				{
	
					int a = getUnusedIndex(Global.EMAIL_OTHER, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
			}
			return;
		}
			
		if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
					
				int type = Integer.parseInt(data[2]); 
				
				if (type == StructuredPostal.TYPE_HOME)
				{				
					if (mappingsGeneric.getConcatAddress())
					{
						int a = getUnusedIndex(Global.ADDRESS_HOME, exportFields);
						if (a != -1)
						{
							exportFields[a] = data[1];
						}
					} else {
						String[] addr = data[1].split("\n");
						for (int i = 0; i < addr.length; i++)
						{
							int b = getUnusedIndex(Global.ADDRESS_HOME, exportFields);
							if (b != -1)
							{
								exportFields[b] = addr[i];
							}
						}
					}
				}
				
				if (type == StructuredPostal.TYPE_WORK)
				{
					if (mappingsGeneric.getConcatAddress())
					{
						int a = getUnusedIndex(Global.ADDRESS_WORK, exportFields);
						if (a != -1)
						{
							exportFields[a] = data[1];
						}
					} else {
						String[] addr = data[1].split("\n");
						for (int i = 0; i < addr.length; i++)
						{
							int b = getUnusedIndex(Global.ADDRESS_WORK, exportFields);
							if (b != -1)
							{
								exportFields[b] = addr[i];
							}
						}
					}
				}
				if (type == StructuredPostal.TYPE_OTHER)
				{
					// Logs.myLog("Address Other = " +  data[1],1);
					if (mappingsGeneric.getConcatAddress())
					{
						int a = getUnusedIndex(Global.ADDRESS_OTHER, exportFields);
						if (a != -1)
						{
							exportFields[a] = data[1];
						}
					} else {
						String[] addr = data[1].split("\n");
						for (int i = 0; i < addr.length; i++)
						{
							int b = getUnusedIndex(Global.ADDRESS_OTHER, exportFields);
							if (b != -1)
							{
								exportFields[b] = addr[i];
							}
						}
					}
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int a = getUnusedIndex(Global.WEBSITE, exportFields);
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
				int a = getUnusedIndex(Global.NOTES, exportFields);
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
				int type = Integer.parseInt(data[2]); 
					
				if (type == Im.TYPE_OTHER)
				{
					int a = getUnusedIndex(Global.IM, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int type = Integer.parseInt(data[2]); 
					
				if (type == Nickname.TYPE_DEFAULT)
				{
					int a = getUnusedIndex(Global.NICKNAME, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
			}
			return;
		}
		
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int type = Integer.parseInt(data[2]); 
					
				if (type == ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
				{
					int a = getUnusedIndex(Global.COMPANY, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int type = Integer.parseInt(data[2]); 
					
				if (type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
				{
					int a = getUnusedIndex(Global.BIRTHDAY, exportFields);
					if (a != -1)
					{
						exportFields[a] = data[1];
					}
				}
			}
			return;
		}
		
		if (mimetype.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE))
		{
			if ((data[1] != null) && (!(data[1].isEmpty())))
			{
				int a = getUnusedIndex(Global.GROUP, exportFields);
				if (a != -1)
				{
					// this is diff because we have to concat all these!
					// Also its a row ID in data1
					String group = getGroup(data[1]);
					if (group != null)
					{
						if (exportFields[a].equals(""))
						{
							exportFields[a] = group;
						} else {
							exportFields[a] = exportFields[a] + "," + group;
						}
					}
				}
			}
			return;
		}
		// vnd.android.cursor.item/phone_v2
		// Based on MIMETYPE you need to decode the rest of the data!!
		/*
		if (data2 != null)
		{
			int x = ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(Integer.parseInt(data2));
			data2 = getResources().getString(x);
		}
		*/
		
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
		
		String exportFields[] = new String[mappingsGeneric.getFieldNum()];
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
					// myLog("Data: " + mimetype + " , " + data1 + "," + data2 + "," + data3 ,2);
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
		
		// Logs.myLog("LINE = " + line,1);
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

		for (int i = 0; i< maxHeaders; i++)
		{
			if (mappingsGeneric.getField(i) != -1)
			{
				header = header + "\"" + Global.getContactField(mappingsGeneric.getField(i)) + "\",";
			}
		}
		header = header.substring(0, header.length() - 1);
		header = header  + "\r\n";
		Logs.myLog("Header: " + header, 2);
		
		
		if (mappingsGeneric.getIgnoreFirstLine() == 1) // slightly odd logic because this var is reused
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
					byte[] data = lines[k].getBytes(settings.getCodePageText());
					fos.write(data);
					n++;
				}
				// displayProgress(k, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logs.myLog("Unable to write to file: " + e.toString(),1);
				// displayProgress(n,"Error:\n\n" + displayName);
				// displayProgress(k,"Error writing contact: " + n);
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
		
		// Get ALL contacts from a specific account
		/*
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
			.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
			.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
			.build();
		*/
		
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, settings.getAccountName())
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, settings.getAccountType())
				.build();
	
		String selection = ContactsContract.Data.MIMETYPE + "=" + DatabaseUtils.sqlEscapeString(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		Cursor cur = cr.query(uri, null, selection, null, sortOrder);
		
		// Cursor cur = cr.query(rawContactUri,null, null, null, sortOrder);

		int num = cur.getCount();
		String exportLines[] = new String[Global.numContacts(settings.getAccountName(),settings.getAccountType(), false)]; // save upto x contacts
		// String exportLines[] = new String[num]; // save upto x contacts

		int n = 0;
		if ( num > 0) {
			while (cur.moveToNext()) {
				String contact = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
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

	
	protected int getUnusedIndexMax(int v, String [] exportFields)
	{
		
		for (int i = 0; i < mappingsGeneric.getFieldNum(); i++) // use contacts not import...
		{
			if (mappingsGeneric.getField(i) == v)
			{
				if (exportFields[i].equals(""))
				{
					// Logs.myLog("Field " + i + " = " + settings.getContactField(v),1);
					return i;
				} else {
					if(v == Global.GROUP) // only field we re-use
					{
						return i;
					}
				}
			}
			
		}
			
		for (int j = 0; j < mappingsGeneric.getFieldNum(); j++)
		{
			if (mappingsGeneric.getField(j) == -1)
			{
				mappingsGeneric.setField(j,v); // make this a new column
				// Logs.myLog("Field " + j + " = " + settings.getContactField(v),1);
				return j; 
			}
		}

		return -1;
	}
	
	
	protected int getUnusedIndexMin(int v, String [] exportFields)
	{
		for (int i = 0; i < maxHeaders; i++)
		// for (int i = 0; i < maxHeaders; i++) // how do I limit for mas headers?
		{
			if (mappingsGeneric.getField(i) == v)
			{
				if (exportFields[i].equals(""))
				{
					return i;
				}
			}
			
		}
		return -1;
	}
	

	
	protected void displayProgress(int n, String s)
	{
		//override this
	}
	
	protected int getMaxHeaders()
	{
		//override this
		return 0;
	}
	
}
