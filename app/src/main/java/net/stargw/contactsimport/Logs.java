package net.stargw.contactsimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

public class Logs {

	private static String logBuffer = ""; // put time in here
	
	private static int LoggingLevel = 1;
	private static final String TAG = "ContactsImportLog";
	private static FileOutputStream logFile;
	
	public Logs() {
		// TODO Auto-generated constructor stub

	}

	public static File XcopyLogFile()
	{
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		
		File tempFile = new File(Global.getContext().getCacheDir() , "logfile.txt");
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		if (Global.copyFile(file, tempFile) == false)
		{
			tempFile = null;
		}
		
		return tempFile;
	}
	
	public static File copyLogFile()
	{
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		
		try {
			logFile.close();
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error closing log file");
		}
		
		File tempFile = new File(Global.getContext().getExternalCacheDir() , "logfile.txt");
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		if (Global.copyFile(file, tempFile) == false)
		{
			tempFile = null;
		}
		
		// reopen log file
		try {
			logFile = new FileOutputStream(file, true);
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error appending to log file: " + file.getAbsolutePath());
		}
		return tempFile;
	}
	
	public static File getLogFile()
	{
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		return file;
	}
	
	public static ArrayList<String> getLogBufferList()
	{
		ArrayList<String> logBuffer = new ArrayList<String>();
		
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
	
			while ((line = br.readLine()) != null) {
				logBuffer.add(line);
			}
			Logs.myLog("Loaded log file: " + file,1);
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.w(TAG, "Error opening log file: " + e);
		}

		Collections.reverse(logBuffer);
		return logBuffer;

	}
	
	public String getLogBuffer()
	{
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		
		try {
			logFile.close();
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error closing log file");
		}
		
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		byte[] buffer = null;
		FileInputStream is;
		
		try {
			is = new FileInputStream(file);
			int size = is.available();
			
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.w(TAG, time.format("%H:%M:%S") + ": Error opening log file: " + e);
		}
		
		try {
			logFile = new FileOutputStream(file, true);
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error appending to log file: " + file.getAbsolutePath());
		}
		
		return new String(buffer);
	}
	
	
	public static void setLoggingLevel(int i)
	{
		LoggingLevel = i;

		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext());
		p.edit().putInt("LoggingLevel", LoggingLevel).commit();
	}

	
	public static int getLoggingLevel()
	{
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext());
		LoggingLevel = p.getInt("LoggingLevel",1);
		
		return LoggingLevel;

	}
	
	public static void setDateStamp()
	{
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		try {
			// file.createNewFile();
			if (file.length() > 1000000)
			{
				logFile = new FileOutputStream(file, false);
			} else { 
				logFile = new FileOutputStream(file, true);
			}
			logBuffer = "\n[" + time.format("%A %d %b %Y at %H:%M:%S") + "] App Started.\n";
			byte[] data = logBuffer.getBytes(); 
			logFile.write(data);
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error creating log file: " + file.getAbsolutePath());
		}
	}
	
	public static void clearLog()
	{
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		
		File file = new File(Global.getContext().getFilesDir(), "logfile.txt");
		
		try {
			logFile.close();
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error closing log file");
		}
		
		try {
			file.createNewFile();
			logFile = new FileOutputStream(file);
			logBuffer = "[" + time.format("%A %d %b %Y at %H:%M:%S") + "] Log Cleared.\n";
			byte[] data = logBuffer.getBytes(); 
			logFile.write(data);
		} catch (Exception e) {
			Log.w(TAG, time.format("%H:%M:%S") + ": Error creating log file: " + file.getAbsolutePath());
		}
	}
	
	//
	// Logs are very important!
	//
	public static void myLog(String buf,int level)
	{
		if (getLoggingLevel() >= level)
		{
			Time time = new Time(Time.getCurrentTimezone());
			time.setToNow();
			
			Log.w(TAG, time.format("%H:%M:%S") + ": " + buf);
			if (logFile != null)
			{
				logBuffer = time.format("%H:%M:%S") + ": " + buf + "\n"; 
				byte[] data = logBuffer.getBytes(); 
				try {
					logFile.write(data);
				} catch (Exception e) {
					Log.w(TAG, time.format("%H:%M:%S") + ": Error writing to log file.");
				}
			}
		}
	}
	
}
