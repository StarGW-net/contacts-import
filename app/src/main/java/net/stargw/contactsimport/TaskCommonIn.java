package net.stargw.contactsimport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.stargw.contactsimport.utils.CSVReader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

public class TaskCommonIn extends AsyncTask<Void, String, Integer> {


	public TaskCommonIn() {
		// TODO Auto-generated constructor stub
	}

	Context context;
	
	@Override
	protected Integer doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void displayProgress(int val, String buf){
		// Nothing we override this
	}
	
	//
	//
	// Utility and common routines below here
	//

	
	//
	// Create a dedicated account to store contacts in
	//
	protected void CreateAccount(Context c)
	{

		int found = 0;
		AccountManager am = AccountManager.get(c.getApplicationContext());
		
		Account[] accounts = am.getAccounts();
		for (Account acc : accounts){
			Logs.myLog("Account name = " + acc.name + ", type = " + acc.type,2);
			if ((acc.type.equals(Global.accountType)) && (acc.name.equals(Global.accountName)))
			{
				Logs.myLog("Found Account name = " + acc.name + ", type = " + acc.type,1);
				found = 1;
				break;
			}
		}

		if (found == 0)
		{
			Logs.myLog("Create Account = " + Global.accountName + ", type = " + Global.accountType,1);
			final Account account = new Account(Global.accountName,Global.accountType);
			am.addAccountExplicitly(account, null, null);
			Logs.myLog("Created Account = " + Global.accountName + ", type = " + Global.accountType,1);
		}
		
	}


	
	//
	// Add a new group
	//
	protected void AddGroup(Context c, String group) {

		// Prepare contact creation request
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		ops.add(ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI)
				.withValue(ContactsContract.Groups.TITLE,group)
				.withValue(ContactsContract.Groups.SOURCE_ID,Global.accountType + "-" + group)
				.withValue(ContactsContract.Groups.GROUP_VISIBLE, 1)
				.withValue(ContactsContract.Groups.ACCOUNT_NAME, Global.accountName)
				.withValue(ContactsContract.Groups.ACCOUNT_TYPE, Global.accountType)
				.build());

		try {
			c.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			Logs.myLog("ADDED Group: " + group,2);

		} catch (Exception e) {
			Logs.myLog("Error adding group: " + group + "(" + e.toString() + ")",1);
		}

	}


	// 
	// Check if the group exists in the account already
	//
	protected int CheckGroup(Context c, String group) {

		int gid = 0;

		Logs.myLog("Checking group: " + group,2);
		
		ContentResolver cr = c.getContentResolver();

		// Cursor cur = cr.query(ContactsContract.Groups.CONTENT_URI, GROUP_PROJECTION, null, null, ContactsContract.Groups.TITLE + " ASC");
		// Cursor cur = cr.query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);

		Uri ContactGroupUri = ContactsContract.Groups.CONTENT_URI;

		Uri AddGroupUri = ContactGroupUri.buildUpon()
		.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
		.appendQueryParameter(RawContacts.ACCOUNT_NAME, Global.accountName)
		.appendQueryParameter(RawContacts.ACCOUNT_TYPE, Global.accountType)
		.build();

		
		// Get ALL contacts from a specific account
		Cursor cur = cr.query(AddGroupUri,null, null, null, null);
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				String id2 = cur.getString(cur.getColumnIndex(ContactsContract.Groups.SOURCE_ID));					

				if (id2 != null)
				{
					if (id2.equals(Global.accountType + "-" + group) == true)
					{
						gid = 1;
						break;
					}
				}
			}
		}

		cur.close(); 

		return gid;
	}
	
	//
	// Loop through adding contacts -- supply argument??
	//
	protected int AddContacts(Context context, List<String[]>  folk, Mappings map)	    
	{
		int givenNameIndex = 0;
		int familyNameIndex = 0;
		boolean givenName = false;
		boolean familyName = false;
				
		for (int i = 0; i < map.getFieldNum(); i++)
		{
			
			if (map.getField(i) == Global.FAMILY_NAME)
			{
				familyNameIndex = i;
				familyName = true;
			}
			
			if (map.getField(i) == Global.GIVEN_NAME)
			{
				givenNameIndex = i;
				givenName = true;
			}
		}
		

		
		Iterator<String[]> iterator = folk.iterator();
		
		if (map.getIgnoreFirstLine() == 1)
		{
			iterator.next();
		}
		
		int good = 0;
		int num = 0;
		while ( iterator.hasNext() ){
			String[] contactRecord = iterator.next();
			
			String displayName = "";
			
			
			if (givenName != false)
			{
				displayName = displayName + contactRecord[givenNameIndex].trim() + " ";
			}
					
					
			if (familyName != false)
			{
				displayName = displayName + contactRecord[familyNameIndex].trim();
			}
			
			
			num++;
			if (displayName.length() < 1)
			{
				displayName = "No Name Contact";
			}

			if (AddContact(context, contactRecord, map) == true)
			{
				// displayProgress(num,"Added:\n\n" +displayName);
				displayProgress(num,displayName);
				good++;
			} else {
				// displayProgress(num,"Error:\n\n" + displayName);
				displayProgress(num,"Error: " + displayName);
			}

			
			if (isCancelled())
			{
				return good;
			}
			
		}
		
		return good;
	}
	
	//
	// Loop through adding contacts -- supply argument??
	//
	protected int AddContactsOld(Context context, List<String[]>  folk, Mappings map)	    
	{
		int givenNameIndex = 0;
		int familyNameIndex = 0;
		boolean givenName = false;
		boolean familyName = false;
				
		for (int i = 0; i < map.getFieldNum(); i++)
		{
			
			if (map.getField(i) == Global.FAMILY_NAME)
			{
				familyNameIndex = i;
				familyName = true;
			}
			
			if (map.getField(i) == Global.GIVEN_NAME)
			{
				givenNameIndex = i;
				givenName = true;
			}
		}
		
		if ((givenName == false) && (familyName == false))
		{
			Logs.myLog("No Given Name or Family Name fileds defined. Without these key fields no records will be imported.",1);
			return 0;
		}
		
		Iterator<String[]> iterator = folk.iterator();
		
		if (map.getIgnoreFirstLine() == 1)
		{
			iterator.next();
		}
		
		int good = 0;
		int num = 0;
		while ( iterator.hasNext() ){
			String[] contactRecord = iterator.next();
			
			String displayName = (contactRecord[givenNameIndex] + " " + contactRecord[familyNameIndex]).trim();
			
			num++;
			if (displayName.length() > 0)
			{
				if (AddContact(context, contactRecord, map) == true)
				{
					// displayProgress(num,"Added:\n\n" +displayName);
					displayProgress(num,displayName);
					good++;
				} else {
					// displayProgress(num,"Error:\n\n" + displayName);
					displayProgress(num,"Error: " + displayName);
				}
			} else {
				Logs.myLog("No display name skipping record: " + num,1);
			}
			
			if (isCancelled())
			{
				return good;
			}
			
		}
		
		return good;
	}
	
	
	//
	// Add a single contact
	//
	protected boolean AddContact(Context context, String[] contactRecord, Mappings map)
	{

		String[] contact = contactRecord;
		
		Logs.myLog("Number of fields: " + contact.length,2);

		// Prepare contact creation request
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		int rawContactInsertIndex = ops.size();

		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.AGGREGATION_MODE,ContactsContract.RawContacts.AGGREGATION_MODE_SUSPENDED)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, Global.accountName)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Global.accountType)
				.withValue(ContactsContract.RawContacts.SYNC1, "1")
				.build());

		
		String givenName = "";
		String familyName = "";
		String middleName = "";
		String homeAddress = "";
		String workAddress = "";
		String otherAddress = "";

		String deferGroups = "";
		
		int max = 0;
		if (contact.length > map.getFieldNum())
		{
			max = map.getFieldNum();
		} else {
			max = contact.length;
		}
		
		for (int i = 0; i < max; i++)
		{
			Logs.myLog("ADDing - field " + (i+1) + " = " + Global.getContactField(map.getField(i)) + " = " + contact[i],3);
			

			
			if (!(contact[i] != null && !contact[i].isEmpty())  )
			{
				Logs.myLog("SKIP - field " + (i+1) +  " = " + Global.getContactField(map.getField(i)) + " is empty!",3);
				continue;
			}

			if ( contact[i].equals("\n"))
			{
				Logs.myLog("SKIP - field " + (i+1) +  " = " + Global.getContactField(map.getField(i)) + " contains only newline!",3);
				continue;
			}
			
			
			// Skip unused fields quickly
			if (map.getField(i) == Global.NONE)
			{
				continue;
			}
			
			if (map.getField(i) == Global.FAMILY_NAME)
			{
				familyName = contact[i];
				continue;
			}
			
			if (map.getField(i) == Global.GIVEN_NAME)
			{
				givenName = contact[i];
				continue;
			}
			
			if (map.getField(i) == Global.MIDDLE_NAME)
			{
				middleName = contact[i];
				continue;
			}

			/*
			Drawable icon = context.getResources().getDrawable(R.drawable.sbeer2);

			Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bitmapdata = stream.toByteArray();

			try {
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
						// .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,EntityUtils.toByteArray(response.getEntity()))
						.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,bitmapdata)
						.build());
			} catch (Exception e) {
				Logs.myLog("Error saving Steve photo!" ,3);
			}
			*/


			if (map.getField(i) == Global.PHONE_WORK)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_WORK)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}	
			
				
			if (map.getField(i) == Global.PHONE_HOME)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_HOME)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
			if (map.getField(i) == Global.PHONE_OTHER)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_OTHER)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
				
			if (map.getField(i) == Global.MOBILE)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_MOBILE)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
			if (map.getField(i) == Global.MOBILE_PERSONAL)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_CUSTOM)
						.withValue(ContactsContract.CommonDataKinds.Phone.LABEL, Global.getContactField(Global.MOBILE_PERSONAL))
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());   
				continue;
			}
			
			if (map.getField(i) == Global.MOBILE_WORK)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_WORK_MOBILE)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());   
				continue;
			}

			if (map.getField(i) == Global.FAX)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	Phone.TYPE_FAX_WORK)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
					
			if (map.getField(i) == Global.EMAIL_HOME)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Email.DATA,	contact[i])
						.withValue(ContactsContract.CommonDataKinds.Email.TYPE,	Email.TYPE_HOME)
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
					
			if (map.getField(i) == Global.EMAIL_WORK)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE,Email.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Email.DATA,	contact[i])
						.withValue(ContactsContract.CommonDataKinds.Email.TYPE,	Email.TYPE_WORK)
						.withValue(ContactsContract.Data.SYNC1, "3")						
						.build());
				continue;
			}
				
			if (map.getField(i) == Global.EMAIL_OTHER)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Email.DATA,	contact[i])
						.withValue(ContactsContract.CommonDataKinds.Email.TYPE,	Email.TYPE_OTHER)
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
				
			if (map.getField(i) == Global.NOTES)
			{
				Logs.myLog("NOTE length =  " + contact[i].length(),3);

				try {
						String z = String.format("%x", new BigInteger(1, contact[i].getBytes("UTF-8")));
					Logs.myLog("NOTE HEX =  " + z,3);
				} catch (Exception e) {
					Logs.myLog("NOTE - cannot convert to HEX",3);
				}
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Note.NOTE,	contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
			if (map.getField(i) == Global.NICKNAME)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Nickname.TYPE,	Nickname.TYPE_DEFAULT)
						.withValue(ContactsContract.CommonDataKinds.Nickname.DATA,	contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
			if (map.getField(i) == Global.IM)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Im.TYPE, Im.TYPE_OTHER)
						.withValue(ContactsContract.CommonDataKinds.Im.DATA, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}

			if (map.getField(i) == Global.BIRTHDAY)
			{
				// Take care of an Exchange oddity
				if  ((!(contact[i].equals("0/0/00"))) &&
						(!(contact[i].equals("0000-00-00"))) )
				{
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
							.withValue(ContactsContract.CommonDataKinds.Event.DATA, contact[i])
							.withValue(ContactsContract.Data.SYNC1, "3")
							.build());
				}
				continue;
			}
			
			if (map.getField(i) == Global.COMPANY)
			{
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
						.withValue(ContactsContract.CommonDataKinds.Organization.DATA, contact[i])
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
				continue;
			}
			
			if (map.getField(i) == Global.WEBSITE)
			{
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE)
							.withValue(ContactsContract.CommonDataKinds.Website.URL, contact[i])
							.withValue(ContactsContract.Data.SYNC1, "3")
							.build());
					continue;
			}
			

			if ( (map.getField(i) == Global.ADDRESS_HOME) )
			{
				if (map.getConcatAddress() == true )
				{
						Logs.myLog("ADD - homeAddress " + contact[i],3);
						if (homeAddress.isEmpty())
						{
							homeAddress = contact[i];
						} else {
							homeAddress = homeAddress + "\n" + contact[i];
						}
				} else {
					// Home Address
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,	StructuredPostal.TYPE_HOME)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,	contact[i])
							.withValue(ContactsContract.Data.SYNC1, "3")
							.build());
				}
				continue;
			}
		
			if ( (map.getField(i) == Global.ADDRESS_WORK) )
			{
				if (map.getConcatAddress() == true )
				{
					if (workAddress.isEmpty())
					{
						workAddress = contact[i];
					} else {
						workAddress = workAddress + "\n" + contact[i];
					}
				} else {
					// Work Address
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,	StructuredPostal.TYPE_WORK)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,	contact[i] + "  ")
							.withValue(ContactsContract.Data.SYNC1, "3")
							.build());
				}
				continue;
			}
			
			if ( (map.getField(i) == Global.ADDRESS_OTHER) )
			{
				if (map.getConcatAddress() == true )
				{
					if (otherAddress.isEmpty())
					{
						otherAddress = contact[i];
					} else {
						otherAddress = otherAddress + "\n" + contact[i];
					}
				} else {
					// Other Address
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,	StructuredPostal.TYPE_OTHER)
							.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,	contact[i] + "  ")
							.withValue(ContactsContract.Data.SYNC1, "3")
							.build());
				}
				continue;
			}
					

			
			if (map.getField(i) == Global.GROUP)
			{
				Logs.myLog("Contact has Group: " + contact[i],3);

				deferGroups = deferGroups + "," + contact[i];

				// We will add he group later...
			}

			if (map.getField(i) == Global.PHOTO)
			{

				byte[] photo = null;

				Settings settings = new Settings();
				settings.load("import");

				File file = new File(settings.getCSVFile());

				Logs.myLog("Photo - CSV File: " + settings.getCSVFile(),3);

				Logs.myLog("Parent: " + file.getParent(),3);
				String dir = file.getParent();
				if (dir == null) {
					Logs.myLog("Photo - iPath: " + Global.iPath,3);
					file = new File(Global.iPath);
					dir = file.getParent().replace("/root/","/");
					Logs.myLog("Photo - getParent: " + dir,3);
					/*
					Logs.myLog("Photo - getPath: " + file.getPath(),3);
					Logs.myLog("Photo - getAbsolutePath: " + file.getAbsolutePath(),3);
					try {
						Logs.myLog("Photo - getCanonicalPath: " + file.getCanonicalPath(),3);
					} catch (IOException e) {
						e.printStackTrace();
					}
					 */

				}

				Logs.myLog("Photo - DIR: " + dir,3);


				File queryImg = new File(dir,contact[i]);
				
				int imageLen = (int)queryImg.length();
				byte [] imgData = new byte[imageLen];
				try {
					FileInputStream fis = new FileInputStream(queryImg);
					fis.read(imgData);
					photo = imgData;
					fis.close();
				} catch (IOException e) {
					Logs.myLog("Photo File: " + queryImg.getAbsolutePath() + " : " + e,2);
				}
				
				try {
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
							.withValue(ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
									// .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,EntityUtils.toByteArray(response.getEntity()))
									.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,photo)
									.build());
				} catch (Exception e) {
					Logs.myLog("Error saving photo: " + queryImg.getAbsolutePath() + " :" + e.toString(),1);
				}
			}

		}

		String displayName = (givenName + " " + familyName).trim();
		
		// Display Name
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
				.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,	familyName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,	givenName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,	middleName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
				.withValue(ContactsContract.Data.SYNC1, "2")
				.build());
	
		// Home Address
		if (map.getConcatAddress() == true ) 
		{
			if (!(homeAddress.isEmpty())) {
				Logs.myLog("Home Address: " + homeAddress,3);
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, StructuredPostal.TYPE_HOME)
						// .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,	homeAddress.replaceAll("\\n$","").trim())
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, homeAddress)
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
			}
		}	
		
		
		// Work Address
		if (map.getConcatAddress() == true ) 
		{
			if (!(workAddress.isEmpty())) {
				Logs.myLog("Work Address: " + workAddress,3);
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, workAddress)
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
			}
		}
			
		// Other Address
		if (map.getConcatAddress() == true ) 
		{
			if (!(otherAddress.isEmpty())) {
				Logs.myLog("Other Address: " + otherAddress,3);
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)
						.withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, otherAddress)
						.withValue(ContactsContract.Data.SYNC1, "3")
						.build());
			}
		}
		
		// Ask the Contact provider to create a new contact
		try {
			ContentProviderResult[] res = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

			String uri = "";
			if (res != null && res[0] != null)
			{
				uri = res[0].uri.getPath().substring(14);
				Logs.myLog("Raw Contact Id: " + uri,3);
			}

			Logs.myLog("ADDED: " + displayName + "\n-------------",2);
			
			// Deffer adding group
			if (!(deferGroups.equals("") == true))
			{
				deferGroups = deferGroups.substring(1, deferGroups.length());

				Logs.myLog("Contact has defered Group: " + deferGroups,3);
				// Add to group creating group if needed
				try {
					CSVReader reader = new CSVReader(new StringReader(deferGroups) );
					String [] nextLine;
					while ((nextLine = reader.readNext()) != null) {
						for (int k=0; k<nextLine.length; k++) {
							String group = nextLine[k].trim();
							if (CheckGroup(context, group) == 0) {
								AddGroup(context, group);
							}
							// Add to an existing Group
							ArrayList<ContentProviderOperation> ops2 = new ArrayList<ContentProviderOperation>();
/*
							ops2.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
									.withValue(ContactsContract.RawContacts.AGGREGATION_MODE,ContactsContract.RawContacts.AGGREGATION_MODE_SUSPENDED)
									.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, Global.accountName)
									.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Global.accountType)
									.withValue(ContactsContract.RawContacts.SYNC1, "1")
									.build());
*/
							ops2.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
									.withValue(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, uri)
									.withValue(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
									.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID, Global.accountType + "-" + group)
							//		.withValue(ContactsContract.Groups.ACCOUNT_NAME, Global.accountName)
							//		.withValue(ContactsContract.Groups.ACCOUNT_TYPE, Global.accountType)
									.withValue(ContactsContract.Data.SYNC1, "3")
									.build());

							ContentProviderResult[] res2 = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops2);
							Logs.myLog("Adding to Group: " + group,1);
						}
					}
				}catch (Exception e) {
					Logs.myLog("Error adding to Group: " + e.toString(),1);
				}
			}

		} catch (Exception e) {
			Logs.myLog("Error saving Contact: " + displayName + " " + e.toString(),1);
			// displayProgress(0,"Error:\n\n" + displayName);
			return false;
		}
		
		return true;
	}
	
}
