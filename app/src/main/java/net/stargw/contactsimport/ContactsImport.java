package net.stargw.contactsimport;

import java.io.File;
import java.util.List;

import java.util.Vector;

// import android.app.FragmentTransaction;
// import android.app.Fragment;
// import android.app.FragmentManager;
// import android.app.FragmentTransaction;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
// import android.support.v4.view.PagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;


public class ContactsImport extends FragmentActivity {


	Context context;

	int state = 1;

	// private Handler mHandler;

	// private LicenseCheckerCallback mLicenseCheckerCallback;
	// private LicenseChecker mChecker;

	private PagerAdapter mPagerAdapter;

	// private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqeveJ9/3++Cmh4zKAkODIv7znwPXBc6p0tFpuCGRAUgqe4Reh5rm2eR55VaZoEFeMUDwId1niu5teYbjJ+DZpwyj0ez1m9wvbyMVofJOAxBP7g2YtuhCN6XmmiSdZpk6xx0ieWmUqrSiUjRZQ4TDHT+FDqG96fP7vWntFZpzuySexN0OFhyGalwXObiCOrsHNnY/cpp/iwy4m9tQKjuIHa5zHF0QacWqYy4sCbgkbnkTIRuSnxKLegIK8sOC/AeJyIC2Mn2E0AVAmLXqZJHmy/oXK6aVB9XUenZ98hCxJMxJFpl/cH/C2sVDZIJT3T57NaA7cVPUXAAbJ6gvv/adIQIDAQAB";  // Your public licensing key.;
	// private static final byte[] SALT = new byte[] { 5, 4, 7, 34, 23, 89, 32, 77, 22, 55, 12, 28, 4, 2, 8 , 45, 56 , 88, 32, 37};


	@Override
	public void onDestroy()
	{
		Logs.myLog("Parent Frgament Activity Destroyed!",3);
		super.onDestroy();
		// clean cache?

		// mChecker.onDestroy();

	}

	// Request code for READ_CONTACTS. It can be any number > 0.
	private static final int PERMISSIONS_CONTACTS_CREATE = 100;

	private static final int PERMISSIONS_CONTACTS_RESUME = 101;

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		Logs.myLog("Requested Permissions: " + requestCode,1);

		switch(requestCode) {
			case PERMISSIONS_CONTACTS_CREATE:
				if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED) ) {
					// same stuff as we do in onResume
					// Create fragments and add to list
					CreateAccount(context);

					List<Fragment> fragments = new Vector<Fragment>();
					fragments.add(Fragment.instantiate(this, FragmentImport.class.getName()));
					fragments.add(Fragment.instantiate(this, FragmentBackup.class.getName()));
					fragments.add(Fragment.instantiate(this, FragmentExport.class.getName()));
					fragments.add(Fragment.instantiate(this, FragmentDelete.class.getName()));
					fragments.add(Fragment.instantiate(this, FragmentView.class.getName()));
					fragments.add(Fragment.instantiate(this, FragmentHelp.class.getName()));
					this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);

					ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
					pager.setOffscreenPageLimit(1);
					pager.setAdapter(this.mPagerAdapter);
					pager.setOnPageChangeListener(pageChangeListener);

					Settings settings = new Settings();
					settings.save("export");
					settings.save("del");
					settings.save("view");

				} else {
					// Toast.makeText(this, "Until you grant the permission, contacts cannot be displayed", Toast.LENGTH_SHORT).show();
					state = 1;
					Global.terminateMessage(context, "Permissions Error 1", "Until you grant READ and WRITE Contacts Permissions, app will not function!");
				}
				break;

			case PERMISSIONS_CONTACTS_RESUME:
				if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
					// same stuff as we do in onResume
					Global.activityResumed();
					Global.clearAccountTotals();
					// Create account and count contacts here?
					// Global.numContacts(Global.accountName, Global.accountType, false);
					Global.numContacts(Global.accountName, Global.accountType, true);
				} else {
					// Toast.makeText(this, "Until you grant the permission, contacts cannot be displayed", Toast.LENGTH_SHORT).show();
					Global.terminateMessage(context, "Permissions Error 2", "Until you grant READ and WRITE Contacts Permissions, app will not function!");
				}
				state = 1;
				break;

			default:
				Global.terminateMessage(context, "Permissions Error 3", "Until you grant READ and WRITE Contacts Permissions, app will not function!");
				break;
		}
	}


	@Override
	public void onPause()
	{
		Logs.myLog("Parent Frgament Activity Paused!",3);
		super.onPause();

		if (checkPermissions() == true) {
			Global.activityPaused(ContactsImport.class);
		} else {
			Logs.myLog("No permissions in pause - do nothing!",3);
		}
		
	}
	
	@Override
	public void onResume()
	{
		Logs.myLog("Parent Frgament Activity Resumed!",3);
		super.onResume();

		// onCreate --> onResume ; called one after each other - an async call in onCreate will not hold up onResume!!

		if (checkPermissions() == true) {

			Global.activityResumed();
			Global.clearAccountTotals();
			// Create account and count contacts here?
			// Global.numContacts(Global.accountName, Global.accountType, false);
			Global.numContacts(Global.accountName, Global.accountType, true);
		} // if not will ultimately crash I guess!!

/*
		if ( checkPermissions() == false) {
			Logs.myLog("State = " + state,3);
			if (state != 1) {
				String[] allPerms = {"android.permission.READ_CONTACTS","android.permission.WRITE_CONTACTS"};
				ActivityCompat.requestPermissions(this, allPerms, PERMISSIONS_CONTACTS_RESUME);  // <-- now you are in a dialoge box; program will continue to run :-(
			}
		} else {
			CreateAccount(context);
			Global.activityResumed();
			Global.clearAccountTotals();
			// Create account and count contacts here?
			// Global.numContacts(Global.accountName, Global.accountType, false);
			Global.numContacts(Global.accountName, Global.accountType, true);
		}
*/

	}

	private boolean checkPermissions()
	{
		Logs.myLog("Check permissions!",3);

		int perms = 0;

		boolean ret = false;

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			perms = perms + 1;
		}

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			perms = perms + 1;
		}

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			perms = perms + 1;
		}

		if (perms == 3) {
			ret = true;
		} else {
			ret = false;
		}

		Logs.myLog("Checked permissions: Perms = " + perms + " : ret = " + ret,3);

		return ret;
	}

	/*
	@Override
	public void onTrimMemory(int i)
	{
		Logs.myLog("Memory running low! Val = " + i,1);
		Global.infoMessage(context,"Warning","Memory running low! Val = " + i);
	}
	*/
	
	@Override
	public void onStop()
	{
		Logs.myLog("Parent Frgament Activity Stopped!",3);
		super.onStop();
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	//	super.onCreate(savedInstanceState);
		super.onCreate(null); // never try and restore state
		/*
		mLicenseCheckerCallback = new MyLicenseCheckerCallback();

		String deviceId = null;
	    // get internal android device id
	    try {
	    	deviceId = android.provider.Settings.Secure.getString(this.getContentResolver(),
	                android.provider.Settings.Secure.ANDROID_ID);
	        if (deviceId == null) {
	        	deviceId = "NoAndroidId";
	        }
	    } catch (Exception e) {
	    	
	    }


		// Construct the LicenseChecker with a Policy.
		mChecker = new LicenseChecker(
				getApplicationContext(), new ServerManagedPolicy(getApplicationContext(),
	 			new AESObfuscator(SALT, getPackageName(), deviceId)),
	 			BASE64_PUBLIC_KEY
				);
*/

		state = 1;

		context = this;

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		Logs.setDateStamp();
		
		Logs.myLog("Parent Frgament Activity Created!",3);
		
		// Logs.myLog("Device ID = " + deviceId,1);
	    
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		
		Logs.myLog("Device = " + manufacturer + " " + model,1);

		Global.maxContacts = 50000;

		// Exempt devices
		/*
		if (deviceId.equals("d09a765d09ce6f07"))
		{
			Logs.myLog("Device Exempt from licensing",1);
			Global.maxContacts = 5000;
			// d09a765d09ce6f07
		}
		*/
		
		// STEVE
		File folder = new File(Environment.getExternalStorageDirectory(), getString(R.string.contactsFilesDir));
		File file = new File(folder, ".zed");
		
		if(file.exists())
		{
			// Logs.myLog("Device Exempt from licensing",1);
			Global.maxContacts = 10000;
		}



		// Bundle args = new Bundle();
		// args.putBoolean("first", true);

		if ( checkPermissions() == false) {
			String[] allPerms = {"android.permission.READ_CONTACTS","android.permission.WRITE_CONTACTS","android.permission.READ_EXTERNAL_STORAGE"};   // ,
			ActivityCompat.requestPermissions(this, allPerms, PERMISSIONS_CONTACTS_CREATE);  // <-- now you are in a dialoge box; program will continue to run :-(
		} else {
			CreateAccount(context);

			List<Fragment> fragments = new Vector<Fragment>();
			fragments.add(Fragment.instantiate(this, FragmentImport.class.getName()));
			fragments.add(Fragment.instantiate(this, FragmentBackup.class.getName()));
			fragments.add(Fragment.instantiate(this, FragmentExport.class.getName()));
			fragments.add(Fragment.instantiate(this, FragmentDelete.class.getName()));
			fragments.add(Fragment.instantiate(this, FragmentView.class.getName()));
			fragments.add(Fragment.instantiate(this, FragmentHelp.class.getName()));
			this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);

			ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
			pager.setOffscreenPageLimit(1);
			pager.setAdapter(this.mPagerAdapter);
			pager.setOnPageChangeListener(pageChangeListener);

			Settings settings = new Settings();
			settings.save("export");
			settings.save("del");
			settings.save("view");

		}

		// settings.save("import");


	}
	
	
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		// int currentPosition = 0;

		public void onPageSelected(int newPosition) {

			FragmentLifecycle fragmentToShow = (FragmentLifecycle)mPagerAdapter.getItem(newPosition);
			fragmentToShow.onRefreshFragment();

			// FragmentLifecycle fragmentToHide = (FragmentLifecycle)mPagerAdapter.getItem(currentPosition);
			// fragmentToHide.onPauseFragment();

			// currentPosition = newPosition;
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) { }

		public void onPageScrollStateChanged(int arg0) { }
	};
	
	
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
			// Logs.myLog("Create Account = " + Global.accountName + ", type = " + Global.accountType,1);
			final Account account = new Account(Global.accountName,Global.accountType);
			am.addAccountExplicitly(account, null, null);
			// ContentResolver.setIsSyncable(account, Global.accountName, 1);
		}
		
	}
	

}
