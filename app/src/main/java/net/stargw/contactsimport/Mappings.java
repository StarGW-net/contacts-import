package net.stargw.contactsimport;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Mappings {

	
	public Mappings() {

	}
	
	// Persistent
	private int ignoreFirstLine;
	private boolean concatAddress; // need to work this out. In and out may be diff
	private int profile;

	private int field[] = new int[Global.fields];
	
	
	public int getProfile()
	{
		return profile;
	}
	
	public String getProfileText()
	{
		String options[] = Global.getContext().getResources().getStringArray(R.array.importProfilesFriendly);

		return options[profile];
	}
	
	public void  setProfile(int p)
	{
		profile = p;
	}
	public int getIgnoreFirstLine()
	{
		return ignoreFirstLine;
	}
	
	public void setIgnoreFirstLine(int i)
	{
		ignoreFirstLine = i;
	}
	

	
	public boolean getConcatAddress()
	{
		return concatAddress;
	}
	
	public void setConcatAddress(boolean i)
	{
		concatAddress = i;
	}
	
	public int getField(int i)
	{
		return field[i];
	}
	
	public void setField(int i, int n)
	{
		field[i] = n;
	}
	
	
	public int getFieldNum()
	{
		return field.length;
	}

	//
	// Save preferences
	//
	public void save()
	{
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext().getApplicationContext());

		p.edit().putInt("IgnoreFirstLine", ignoreFirstLine).commit();
		p.edit().putInt("Profile", profile).commit();
		p.edit().putBoolean("ConcatAddress", concatAddress).commit();
		
		
		for (int i = 0; i < field.length; i++)
		{
			p.edit().putInt("Field" + i,field[i] ).commit();
		}
		
	}

	//
	// Load preferences
	//
	public void load()
	{
		// Not all defaults are saved!
		
		// Defaults
		field[0] = Global.GIVEN_NAME;
		field[1] = Global.FAMILY_NAME;

		
		for (int i = 2; i< field.length; i++)
		{
			field[i] = Global.NONE; // How to cope with multiples?
		}
		
		// Prefs
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getContext().getApplicationContext());
		
		for (int i = 0; i < field.length; i++)
		{
			field[i] = p.getInt("Field" + i,field[i]);
		}
		
		
		ignoreFirstLine = p.getInt("IgnoreFirstLine",1);
		profile = p.getInt("Profile",0); // None
		
		concatAddress = p.getBoolean("ConcatAddress",true);

	}

	public void defaultPrefs()
	{
		
		field[0] = Global.GIVEN_NAME;
		field[1] = Global.FAMILY_NAME;

		
		for (int i = 2; i< field.length; i++)
		{
			field[i] = Global.NONE; // How to cope with multiples?
		}

		// profile = 1;
	}
	


	
	public void defaultOutlookEnglishPrefs()
	{
		for (int i = 0; i< field.length; i++)
		{
			field[i] = Global.NONE; // How to cope with multiples?
		}
		
		field[1] = Global.GIVEN_NAME;
		field[2] = Global.MIDDLE_NAME;
		field[3] = Global.FAMILY_NAME;
		
		field[5] = Global.COMPANY;
		
		field[8] = Global.ADDRESS_WORK; // How to cope with multiples?
		field[9] = Global.ADDRESS_WORK;
		field[10] = Global.ADDRESS_WORK;
		field[11] = Global.ADDRESS_WORK;
		field[12] = Global.ADDRESS_WORK;
		field[13] = Global.ADDRESS_WORK;
		field[14] = Global.ADDRESS_WORK;
		
		field[15] = Global.ADDRESS_HOME; // How to cope with multiples?
		field[16] = Global.ADDRESS_HOME;
		field[17] = Global.ADDRESS_HOME;
		field[18] = Global.ADDRESS_HOME;
		field[19] = Global.ADDRESS_HOME;
		field[20] = Global.ADDRESS_HOME;
		field[21] = Global.ADDRESS_HOME;
		
		field[22] = Global.ADDRESS_OTHER; // How to cope with multiples?
		field[23] = Global.ADDRESS_OTHER;
		field[24] = Global.ADDRESS_OTHER;
		field[25] = Global.ADDRESS_OTHER;
		field[26] = Global.ADDRESS_OTHER;
		field[27] = Global.ADDRESS_OTHER;
		field[28] = Global.ADDRESS_OTHER;

		field[30] = Global.FAX;

		field[31] = Global.PHONE_WORK;
		// field[32] = Global.MOBILE_WORK;
		field[32] = Global.MOBILE;
		
		field[37] = Global.PHONE_HOME;
		field[38] = Global.PHONE_HOME;
		
		// field[40] = Global.MOBILE_PERSONAL;
		field[40] = Global.MOBILE;
		field[42] = Global.PHONE_OTHER;
		
		field[52] = Global.BIRTHDAY;
		
		field[54] = Global.GROUP;
		
		field[57] = Global.EMAIL_OTHER;
		field[60] = Global.EMAIL_OTHER;
		field[63] = Global.EMAIL_OTHER;
		
		field[77] = Global.NOTES;
		field[91] = Global.WEBSITE;
		

	}
	
	

	
	public void defaultOutlookSpanishPrefs()
	{
		for (int i = 0; i< field.length; i++)
		{
			field[i] = Global.NONE; // How to cope with multiples?
		}
		
		field[1] = Global.GIVEN_NAME;
		field[2] = Global.MIDDLE_NAME;
		field[3] = Global.FAMILY_NAME;
		
		field[5] = Global.COMPANY;
		
		field[8] = Global.ADDRESS_WORK; // How to cope with multiples?
		field[9] = Global.ADDRESS_WORK;
		field[10] = Global.ADDRESS_WORK;
		field[11] = Global.ADDRESS_WORK;
		field[12] = Global.ADDRESS_WORK;
		field[13] = Global.ADDRESS_WORK;
		field[14] = Global.ADDRESS_WORK;
		
		field[15] = Global.ADDRESS_HOME; // How to cope with multiples?
		field[16] = Global.ADDRESS_HOME;
		field[17] = Global.ADDRESS_HOME;
		field[18] = Global.ADDRESS_HOME;
		field[19] = Global.ADDRESS_HOME;
		field[20] = Global.ADDRESS_HOME;
		field[21] = Global.ADDRESS_HOME;
		
		field[22] = Global.ADDRESS_OTHER; // How to cope with multiples?
		field[23] = Global.ADDRESS_OTHER;
		field[24] = Global.ADDRESS_OTHER;
		field[25] = Global.ADDRESS_OTHER;
		field[26] = Global.ADDRESS_OTHER;
		field[27] = Global.ADDRESS_OTHER;
		field[28] = Global.ADDRESS_OTHER;

		field[30] = Global.FAX;

		field[31] = Global.PHONE_WORK;
		// field[32] = Global.MOBILE_WORK;
		field[32] = Global.MOBILE;
		
		field[37] = Global.PHONE_HOME;
		field[38] = Global.PHONE_HOME;
		
		// field[40] = Global.MOBILE_PERSONAL;
		field[40] = Global.MOBILE;
		field[42] = Global.PHONE_OTHER;
		
		field[54] = Global.BIRTHDAY;
		
		field[55] = Global.GROUP;
		
		field[57] = Global.EMAIL_OTHER;
		field[60] = Global.EMAIL_OTHER;
		field[63] = Global.EMAIL_OTHER;
		
		field[77] = Global.NOTES;
		field[81] = Global.WEBSITE;
		

	}
	
	public void defaultThunderbirdPrefs()
	{
		for (int i = 0; i< field.length; i++)
		{
			field[i] = Global.NONE; //
		}
		
		field[0] = Global.GIVEN_NAME;
		field[1] = Global.FAMILY_NAME;
		
		field[3] = Global.NICKNAME;
		
		field[4] = Global.EMAIL_OTHER;
		field[5] = Global.EMAIL_OTHER;
		
		field[7] = Global.PHONE_WORK;
		field[8] = Global.PHONE_HOME;
		field[9] = Global.FAX;

		field[11] = Global.MOBILE;
		
		field[12] = Global.ADDRESS_HOME;
		field[13] = Global.ADDRESS_HOME;
		field[14] = Global.ADDRESS_HOME;
		field[15] = Global.ADDRESS_HOME;
		field[16] = Global.ADDRESS_HOME;
		field[17] = Global.ADDRESS_HOME;
		
		field[18] = Global.ADDRESS_WORK;
		field[19] = Global.ADDRESS_WORK;
		field[20] = Global.ADDRESS_WORK;
		field[21] = Global.ADDRESS_WORK;
		field[22] = Global.ADDRESS_WORK;
		field[23] = Global.ADDRESS_WORK;
		
		field[26] = Global.COMPANY;
		
		field[27] = Global.WEBSITE;
		field[28] = Global.WEBSITE;
		
		field[36] = Global.NOTES;

	}
	
	public void defaultStevePrefs()
	{
		for (int i = 0; i< field.length; i++)
		{
			field[i] = Global.NONE; //
		}
		
		field[0] = Global.GIVEN_NAME;
		field[1] = Global.FAMILY_NAME;

		field[2] = Global.GROUP;

		field[3] = Global.MOBILE;

		field[4] = Global.PHONE_WORK;
		field[5] = Global.PHONE_HOME;

		field[6] = Global.EMAIL_HOME;
		field[7] = Global.EMAIL_WORK;

		field[8] = Global.ADDRESS_HOME;
		field[9] = Global.ADDRESS_WORK;

		field[10] = Global.NOTES;

		field[11] = Global.WEBSITE;
		
	}

	public void defaultFastmailPrefs()
	{
		for (int i = 0; i< field.length; i++)
		{
			field[i] = Global.NONE; //
		}

		field[1] = Global.GIVEN_NAME;
		field[2] = Global.FAMILY_NAME;

		field[33] = Global.BIRTHDAY;

		field[37] = Global.NOTES;

		field[5] = Global.GROUP;

		field[34] = Global.EMAIL_HOME;
		field[35] = Global.EMAIL_WORK;
		field[36] = Global.EMAIL_OTHER;


		field[26] = Global.PHONE_WORK;
		field[27] = Global.PHONE_WORK;
		field[28] = Global.PHONE_HOME;
		field[29] = Global.PHONE_HOME;

		field[30] = Global.MOBILE;
		field[31] = Global.PHONE_OTHER;

		field[7] = Global.ADDRESS_WORK;
		field[8] = Global.ADDRESS_WORK;

		field[13] = Global.ADDRESS_HOME;
		field[14] = Global.ADDRESS_HOME;

		field[19] = Global.ADDRESS_OTHER;
		field[20] = Global.ADDRESS_OTHER;

		field[6] = Global.PHOTO;

	}


	/*
	void defaultCopyImport()
	{
		Mappings map = new Mappings(context,"import");
		
		map.loadPref();
		
		for (int i = 0; i< field.length; i++)
		{
			field[i] = map.getField(i);
		}
		
	}
	*/
	
	//
	// Check given and family names are there
	//
	public boolean checkKeyFields()
	{				
		boolean givenName = false;
		boolean familyName = false;
		
		for (int i = 0; i < field.length; i++)
		{
			
			if (field[i] == Global.FAMILY_NAME)
			{
				familyName = true;
			}
			
			if (field[i] == Global.GIVEN_NAME)
			{
				givenName = true;
			}
		}

		if ((givenName == false) && (familyName == false))
		{
			return false;
		}
		
		return true;
	}
	

	
}
