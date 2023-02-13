package net.stargw.contactsimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import androidx.core.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Global extends Application {


	// TaskController myTaskController;

	static List<String[]> peopleImport;
	static List<String[]> peopleRecover;
	
	static HashMap<String, Integer> accountTotals = new HashMap<String, Integer>(); 
	
	final static int fields = 101;
	final static int contactFields = 25;
	static int maxContacts = 10000;

	static String iPath = null;

	static String accountName;
	static String accountType;
	
	static private String contactField[] = new String[contactFields];
	static private Boolean contactFieldFlag[] = new Boolean[contactFields];
	
	final static int NONE = 0;
	final static int GIVEN_NAME = 1;
	final static int MIDDLE_NAME = 2;
	final static int FAMILY_NAME = 3;
	final static int ADDRESS_HOME = 4;
	final static int ADDRESS_WORK = 5;
	final static int ADDRESS_OTHER = 6;
	final static int PHONE_WORK = 7;
	final static int PHONE_HOME = 8;
	final static int PHONE_OTHER = 9;
	final static int MOBILE = 10;
	final static int MOBILE_PERSONAL = 11;
	final static int MOBILE_WORK = 12;
	final static int EMAIL_HOME = 13;
	final static int EMAIL_WORK = 14;
	final static int EMAIL_OTHER = 15;
	final static int COMPANY = 16;
	final static int NOTES = 17;
	final static int WEBSITE = 18;
	final static int GROUP = 19;
	final static int BIRTHDAY = 20;
	final static int IM = 21;
	final static int PHOTO = 22;
	final static int NICKNAME = 23;
	final static int FAX = 24;
	
	public static String getContactField(int i)
	{
		return contactField[i];
	}
	
	public static int getContactFieldNum()
	{
		return contactField.length;
	}
	
	public static String[] getContactFieldAll()
	{
		return contactField;
	}
	
	public static boolean getContactFieldFlag(int i)
	{
		return contactFieldFlag[i];
	}
	
	private void setContactFieldsAll()
	{		
		Context context = getContext();
		
		contactField[GIVEN_NAME] = context.getString(R.string.GIVEN_NAME);
		contactField[FAMILY_NAME] = context.getString(R.string.FAMILY_NAME);
		contactField[MIDDLE_NAME] = context.getString(R.string.MIDDLE_NAME);
		contactField[ADDRESS_WORK] = context.getString(R.string.ADDRESS_WORK);
		contactField[ADDRESS_HOME] = context.getString(R.string.ADDRESS_HOME);
		contactField[ADDRESS_OTHER] = context.getString(R.string.ADDRESS_OTHER);
		contactField[PHONE_WORK] = context.getString(R.string.PHONE_WORK);
		contactField[PHONE_HOME] = context.getString(R.string.PHONE_HOME);
		contactField[PHONE_OTHER] = context.getString(R.string.PHONE_OTHER);
		contactField[MOBILE_WORK] = context.getString(R.string.MOBILE_WORK);
		contactField[MOBILE] = context.getString(R.string.MOBILE);
		contactField[MOBILE_PERSONAL] = context.getString(R.string.MOBILE_PERSONAL);
		contactField[MOBILE_WORK] = context.getString(R.string.MOBILE_WORK);
		contactField[EMAIL_HOME] = context.getString(R.string.EMAIL_HOME);
		contactField[EMAIL_WORK] = context.getString(R.string.EMAIL_WORK);
		contactField[EMAIL_OTHER] = context.getString(R.string.EMAIL_OTHER);
		contactField[NOTES] = context.getString(R.string.NOTES);
		contactField[WEBSITE] = context.getString(R.string.WEBSITE);
		contactField[COMPANY] = context.getString(R.string.COMPANY);
		contactField[BIRTHDAY] = context.getString(R.string.BIRTHDAY);
		contactField[GROUP] = context.getString(R.string.GROUP);
		contactField[NONE] = context.getString(R.string.NONE);
		contactField[IM] = context.getString(R.string.IM);
		contactField[PHOTO] = context.getString(R.string.PHOTO);
		contactField[NICKNAME] = context.getString(R.string.NICKNAME);
		contactField[FAX] = context.getString(R.string.FAX);

		for (int i = 0; i < contactField.length; i++)
		{
			contactFieldFlag[i] = true;
		}
		contactFieldFlag[GIVEN_NAME] = false;
		contactFieldFlag[FAMILY_NAME] = false;
		contactFieldFlag[NOTES] = false;
	}
	
	private static Context mContext;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		
		accountName = mContext.getString(R.string.accountName);
		accountType = mContext.getString(R.string.accountType);
		
		setContactFieldsAll();
	}
	

	
	public static Context getContext(){
		return mContext;
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}  
	
	private static boolean activityVisible;
	
	private static String task = null;
	
	
	public static void activityResumed() {
		activityVisible = true;
		
		// works even if there is no notification...
		notificationCancel();
	}
	
	static Class returnActivity = ContactsImport.class;
	
	public static void activityPaused(Class c) {
		activityVisible = false;
		if (task != null)
		{
			notificationTaskRunning(task, c);
			returnActivity = c;
		}
	}
	
	public static void activityDone() {
		notificationTaskFinished(returnActivity);
	}



	
	public static void setTaskText(String t)
	{
		task = t;
		if ((activityVisible == false) && (task != null))
		{
			// change text in notification
			notificationTaskRunning(task, returnActivity);
		}
	}


	
	public static int XcheckContacts(String aName, String aType)
	{
		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, aName)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, aType)
				.build();
	
		String selection = ContactsContract.Data.MIMETYPE + "=" + DatabaseUtils.sqlEscapeString(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		
		String[] projection = new String[] {
				ContactsContract.Data.RAW_CONTACT_ID,
				ContactsContract.Data.CONTACT_ID, 
				ContactsContract.Data.DATA1,
		};
		
		String[] selectionArgs = null;
				
		ContentResolver cr = mContext.getContentResolver();
	
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, null);
		
		int num = cur.getCount();
		
		cur.close();
		
		return num;
	}
	
	public static void clearAccountTotals()
	{
		accountTotals.clear();
	}
	
	public static int numContacts(String aName, String aType, boolean cache)
	{
		String key = aName + ":" + aType;
		
		// Logs.myLog("Counting number of contacts",1);
		
		if (cache == true)
		{
			if (accountTotals.containsKey(key))
			{
				int x = (Integer) accountTotals.get(key); 
				// Logs.myLog("Using OLD Cached number of contacts in account " + key + " = " + x,1);
				return x;
			}
		}

		Uri uri = ContactsContract.Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, aName)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, aType)
				.build();
	
		String selection = ContactsContract.Data.MIMETYPE + "=" + DatabaseUtils.sqlEscapeString(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		
		String[] projection = new String[] {
				ContactsContract.Data.RAW_CONTACT_ID,
				ContactsContract.Data.CONTACT_ID, 
				ContactsContract.Data.DATA1,
		};
		
		String[] selectionArgs = null;
				
		ContentResolver cr = mContext.getContentResolver();
	
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, null);
		
		int num = cur.getCount();
		
		cur.close();
		
		// Logs.myLog("Creating NEW Cache number of contacts in account " + key + " = " + num,1);
		accountTotals.put(key, num); 
		
		return num;
	}
	
	
	//
	// Display a popup info screen
	//
	public static void infoMessage(final Context context, String header, String message)
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
			}
		});
		

		info.show();
		Logs.myLog(header + ":" + message,1);
	}

	//
	// Display a popup info screen - and stop
	//
	public static void terminateMessage(final Context context, String header, String message)
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
				// mContext.getActivity().finish();
				System.exit(0);
			}
		});


		info.show();
		Logs.myLog(header + ":" + message,1);
	}

	//
	// Display a popup info screen
	//
	public static void infoMessage2(final Context context, String header, String message, String button, int action, final String extra)
	{
		final Dialog info = new Dialog(context);

		info.setContentView(R.layout.dialog_info);
		
		info.setTitle(header);
		
		TextView text = (TextView) info.findViewById(R.id.infoMessage);
		text.setText(message);
		text.setGravity(Gravity.CENTER_HORIZONTAL);  
		
		Button dialogButton = (Button) info.findViewById(R.id.infoButton);
		// if button is clicked, close the custom dialog

		
		dialogButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				// notificationCancel(context);
				info.cancel();
			}
		});
		
		Button actionButton = (Button) info.findViewById(R.id.infoButtonAction);
		actionButton.setText(button);
		actionButton.setVisibility(View.VISIBLE);
		
		if (action == 1)
		{
			
			actionButton.setOnClickListener(new OnClickListener() {
				// @Override
				public void onClick(View v) {
					// notificationCancel(context);
					info.cancel();

					Settings settings = new Settings();
					settings.setAccountName(Global.accountName);
					settings.setAccountType(Global.accountType);
					settings.save("view");
					
					// Intent intent = new Intent(context,ActivityViewContacts.class);
					// intent.putExtra("action","viewContacts");
					// context.startActivity(intent);
					
					DialogViewContacts viewContacts = new DialogViewContacts();
					viewContacts.onCreate(context,2, (long) 0);
				}
			});
		}
		if (action == 2)
		{
			// notificationCancel(context);
			actionButton.setOnClickListener(new OnClickListener() {
				// @Override
				public void onClick(View v) {
					info.cancel();
					Intent emailIntent = new Intent(Intent.ACTION_SEND);
					// emailIntent.setData(Uri.parse("mailto:"));
					// emailIntent.setType("text/csv");
					// emailIntent.setType("application/octet-stream");
					emailIntent.setType("plain/text");

					
					File file = new File(extra);
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
					Uri uri = Uri.parse("file://" + extra);
					
					// ArrayList<Uri> u = new ArrayList<Uri>();
					// u.add(uri);
					emailIntent.setType("application/octet-stream");
					emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
					try {
						context.startActivity(emailIntent);
					} catch (Exception e) {
						Logs.myLog("Email export failed! " + e,1);
						Global.infoMessage(mContext,mContext.getString(R.string.warning),mContext.getString(R.string.emailFailed));
					}
				}
			});
		}

		/*
		com.android.contacts.action.FILTER_CONTACTS
		
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Long.toString(n));
		intent.setData(uri);
		startActivity(intent);
		*/
		info.show();
		
	}
	
	
	static void keyFieldWarn(Context context)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
	
		alert.setTitle(mContext.getString(R.string.warning));
		alert.setMessage(mContext.getString(R.string.noKeyFields));
	
		alert.setNegativeButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
	
		alert.show();
	}
	
	public static void getCodePages()
	{

		SortedMap<String, Charset> m = Charset.availableCharsets();
		Set<String> k = m.keySet();
		Logs.myLog("Canonical name, Display name," +" Can encode, Aliases",1);
		Iterator<String> i = k.iterator();
		while (i.hasNext()) 
		{
			String n = (String) i.next();
			Charset e = (Charset) m.get(n);
			String d = e.displayName();
			boolean c = e.canEncode();
			Logs.myLog(n+", "+d+", "+c,1);
			Set s = e.aliases();
			Iterator j = s.iterator();
			while (j.hasNext()) 
			{
				String a = (String) j.next();         
				Logs.myLog(", "+a,1);
			}
			Logs.myLog("",1);
		}
	}


	public static boolean copyFile(File file, File tempFile)
	{
		
		String text = null;
				
		FileInputStream is;
		try {
			is = new FileInputStream(file);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			text = new String(buffer, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logs.myLog("File Error: " + e,1);
			return false;
		}

		// Logs.myLog("Data = " + text,1);
		
		// String[] options = getActivity().getResources().getStringArray(R.array.codePages);
		// String cp = options[mappingsTools.getCodePage()];
		
		// Logs.myLog("Converting backup to Code Page: " + cp,2);
		
		// File tempFile = new File(newFolder, fileName);

		
		try {
			FileOutputStream fos = new FileOutputStream(tempFile);
			
			
			byte[] data = text.getBytes("UTF-8");
		
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logs.myLog("File Error: " + e,1);
			return false;
		}
		tempFile.setReadable(true, false);
		Logs.myLog("Temp file to share: " + tempFile.getAbsolutePath(),2);
		return true;
	}
	
	
	public static void notificationTaskRunning(String task, Class c)
	{
		// cancel any previous
		notificationCancel();
		
		NotificationManager nM;
		
		Intent notificationIntent = new Intent(mContext, c);  
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
			.setSmallIcon(R.drawable.icon_notification_do)
			.setContentTitle(mContext.getString(R.string.app_name))
			.setContentText(task)
			.setContentIntent(pendingIntent);
		Notification notification = mBuilder.build();
		
		// default phone settings for notifications
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.defaults |= Notification.DEFAULT_SOUND;
		
		// cancel notification after click
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// show scrolling text on status bar when notification arrives
		// notification.tickerText = title + "\n" + content;
		
		// notifiy the notification using NotificationManager
		nM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nM.notify(666, notification);
	}
	
	public static void notificationCancel()
	{
		NotificationManager notM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notM.cancel(666);
	}
	
	public static void notificationTaskFinished(Class c)
	{
		// cancel any previous
		notificationCancel();
		
		NotificationManager nM;
		
		Intent notificationIntent = new Intent(mContext, c);  
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_notification_done_large);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
			.setSmallIcon(R.drawable.icon_notification_done)
			.setLargeIcon(bm)
			// .setLights(0xFF0000FF,100,3000)
			// .setPriority(Notification.PRIORITY_DEFAULT)
			.setContentTitle(mContext.getString(R.string.app_name))
			.setContentText(mContext.getString(R.string.finished))
			.setContentIntent(pendingIntent);
		Notification notification = mBuilder.build();
		
		// default phone settings for notifications
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.defaults |= Notification.DEFAULT_SOUND;
		
		// cancel notification after click
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// show scrolling text on status bar when notification arrives
		// notification.tickerText = title + "\n" + content;
		
		// notifiy the notification using NotificationManager
		nM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nM.notify(666, notification);
	}
}
