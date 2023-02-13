package net.stargw.contactsimport;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

	private String csvFile; // used for import. (could use for export, backup and recover??)
	private String accountName;
	private String accountType;
	private int codePage;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		
		accountName = Global.accountName;
		accountType = Global.accountType;
		codePage = 0;
		csvFile = "";
	}
	
	
	public void setCodePage(int i)
	{
		codePage = i;
	}
	
	public int getCodePage()
	{
		return codePage;
	}
	
	public String getCodePageText()
	{
		String options[] = Global.getContext().getResources().getStringArray(R.array.codePages);

		return options[codePage];
	}
	
	
	public String getCSVFile()
	{
		return csvFile;
	}
	
	public void setCSVFile(String s)
	{
		csvFile = s;
	}
	
	public void setAccountName(String a)
	{
		accountName = a;
	}
	
	public String getAccountName()
	{
		return accountName;
	}
	
	public void setAccountType(String a)
	{
		accountType = a;
	}
	
	public String getAccountType()
	{
		return accountType;
	}
	
	//
	// Save preferences
	//
	public void save(String prefix)
	{
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext().getApplicationContext());

		p.edit().putInt(prefix + "-CodePage", codePage).commit();
		
		p.edit().putString (prefix + "-AccountName",accountName).commit();
		p.edit().putString (prefix + "-AccountType",accountType).commit();
		p.edit().putString (prefix + "-CSVfile",csvFile).commit();
		
	}
	
	
	//
	// Load preferences
	//
	public void load(String prefix)
	{
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext().getApplicationContext());

		// csvFile = p.getString (prefix + "-file",Environment.getExternalStorageDirectory() + "/contacts.csv");
		csvFile = p.getString (prefix + "-CSVfile","/"); // Nothing
		
		codePage = p.getInt(prefix + "-CodePage",0);
		
		accountName = p.getString(prefix + "-AccountName", Global.accountName);
		accountType = p.getString(prefix + "-AccountType", Global.accountType);

	}
}
