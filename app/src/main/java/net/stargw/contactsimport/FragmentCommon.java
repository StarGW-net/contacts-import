package net.stargw.contactsimport;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import static android.content.Intent.EXTRA_MIME_TYPES;

public class FragmentCommon extends Fragment {

	

	
	public FragmentCommon() {
		// TODO Auto-generated constructor stub
	}


	
	protected void filePicker(File mPath, final String search, final boolean recurse, final int flag) 
	{
		String[] mFileList;
		
		// Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(getActivity());
		// AlertDialog.Builder builder = new Builder(cw);
		
		builder.setTitle(getString(R.string.selectFile));
		
		// loadFileList(mPath);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				if (recurse == true)
				{
					if (search.equals(".bak"))
					{
						return (filename.toLowerCase().endsWith(search) && filename.toLowerCase().startsWith("backup")) || sel.isDirectory() && (!filename.startsWith("."));
					} else {
						return filename.toLowerCase().endsWith(search) || sel.isDirectory() && (!filename.startsWith("."));
					}
				} else {
					if (search.equals(".bak"))
					{
						return filename.toLowerCase().endsWith(search) && filename.toLowerCase().startsWith("backup") && (!filename.startsWith("."));
					} else {
						return filename.toLowerCase().endsWith(search) && (!filename.startsWith("."));
					}
				}
			}
		};
		mFileList = mPath.list(filter);
		final File path = mPath;
		
		// Sort
		if ((mFileList == null) || (mFileList.length == 0))
		{
			builder.setMessage(String.format(getString(R.string.noFiles),search));
		} else {			
		
			final ListAdapter adapter = sortFileList2(path, mFileList);  // how to set an xml file for the row??
			
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which ) {
					String item = (String) adapter.getItem(which);
					
					File file = new File(path, item);
					
					if (file.isDirectory())
					{
						filePicker(file,search,recurse,flag);
					} else {
						Logs.myLog("Selected file: " + path.getName() + "/" + item,1);
						processFile(file); // actually handles the file - if its an intent we never get here
						// do something!
						/*
						if (flag != 1) {

						} // else just return cos its now an intent...

						 */
					}
				}
			});

		}
		
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		
		if (recurse == true)
		{
			builder.setNeutralButton(getString(R.string.back), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					File d = path.getParentFile();
					// if (!(path.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())))
					if (!(path.getAbsolutePath().equals("/")))
					{
						filePicker(d,search,recurse,flag);
					}
				}
			});
		}
		
		if (flag == 1)  // This should then do an intent
		{

			builder.setPositiveButton(getString(R.string.external), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// filePicker(Environment.getExternalStorageDirectory(),search,true,2);

					Intent intentCSV = new Intent(Intent.ACTION_GET_CONTENT);
					// Intent intentCSV = new Intent(Intent.ACTION_OPEN_DOCUMENT);

					intentCSV.setType("text/*");
					// intentCSV.setType("*/*");
					// intentCSV.setType("file/weight*.csv");

					// String[] mimetypes = {"text/comma-separated-values", "text/csv"};

					String[] mimetypes = {"application/octet-stream"};

					intentCSV.putExtra(EXTRA_MIME_TYPES, mimetypes);

					intentCSV.addCategory(Intent.CATEGORY_OPENABLE);

					Intent chooserIntent = Intent.createChooser(intentCSV, "Import CSV");

					try {
						startActivityForResult(chooserIntent, 1);
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(getContext().getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
					}
				}
			});



		}


		
		if (flag == 2)
		{
			builder.setPositiveButton(getString(R.string.internal), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					filePicker(getActivity().getFilesDir(),search,false,1);
				}
			});
		}
		
		
		if (recurse == true)
		{
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {         
				public void onCancel(DialogInterface dialog) {
					//do whatever you want the back key to do
					File d = path.getParentFile();
					if (!(path.getAbsolutePath().equals("/")))
					// if (!(path.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())))
					{
						filePicker(d,search,recurse,flag);
					}
				}
			});
		}

		builder.show();

	}
	
	private ListAdapter sortFileList2(File path, String [] files)
	{
		String[] newList = new String[files.length];
		
		for (int i = 0; i < files.length; i++)
		{
			File file = new File(path, files[i]);
			if (file.isDirectory())
			{
				newList[i] = "[DIR] " + files[i];
			} else {
				newList[i] = files[i];
			}
		}
		
		List<String> list = Arrays.asList(newList);  
		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		
		list.toArray(newList);
		Integer[] icons = new Integer[newList.length];
		
		for (int k = 0; k< newList.length; k++)
		{
			if (newList[k].contains("[DIR]"))
			{
				newList[k] = newList[k].replace("[DIR] ","");
				icons[k] = R.drawable.icon_dir;
			} else {
				icons[k] = R.drawable.icon_csv;
			}
		}
		
		ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), newList, icons);
		return adapter;
	}
	
	// stub that should get overriden
	void processFile(File file)
	{
		
	}
	
	protected void codePagePicker(final TextView cp)
	{
		final Context context = getActivity();
		
		
		String[] options = context.getResources().getStringArray(R.array.codePagesFriendly);
		
		
		cp.setText(options[codePageLoad()]);
		
		cp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				codePagePickerDropdown(cp);
			}
		});
	}
	
	protected void codePagePickerDropdown(final TextView cp)
	{
		Logs.myLog("Selected picker",1);
		
		final Context context = getActivity();
		
		AlertDialog.Builder builder = new Builder(context);
		
		builder.setTitle(getString(R.string.selectCodePage));
		
		String[] options = context.getResources().getStringArray(R.array.codePagesFriendly);
		
	
		builder.setSingleChoiceItems(options, codePageLoad(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				String[] options = context.getResources().getStringArray(R.array.codePagesFriendly);
				cp.setText(options[item]);
				codePageSave(item);
				// Need to save item here!
				dialog.cancel();
			}});
		
		builder.show();
	}
	
	// Because I need access to the Mappings
	protected void codePageSave(int i)
	{
		// stub to override
	}
	
	protected int codePageLoad()
	{
		// stub to override
		return 0;
	}
	
	protected void accountPicker() 
	{
		
		// Dialog dialog = null;
		// AlertDialog.Builder builder = new Builder(getActivity());

		// builder.setTitle(getString(R.string.selectAccount));
		
		AccountManager am = AccountManager.get(getActivity().getApplicationContext());
		
		Account[] accounts = am.getAccounts();
		String[] accountName = new String[accounts.length];
		String[] accountType = new String[accounts.length];
		// String[] display = new String[accounts.length];
		AccountData[] display = new AccountData[accounts.length];
		
		AuthenticatorDescription[] accountTypes = AccountManager.get(Global.getContext()).getAuthenticatorTypes();

		int n = 0;
		for (Account acc : accounts)
		{
			Logs.myLog("Account name = " + acc.name + ", type = " + acc.type,2);
			accountName[n] = acc.name;
			accountType[n] = acc.type;
			
			// Get friendly display name
			AuthenticatorDescription ad = getAuthenticatorDescription(acc.type, accountTypes);
			
			try {
				PackageManager pm = Global.getContext().getPackageManager();
				String s = pm.getText(ad.packageName, ad.labelId, null).toString();
				display[n] = new AccountData(acc.name,"(" + s + ")");
				// display[n] = s;
			} catch (Exception e) {
				Logs.myLog("No display info for Account Type = " + acc.type + " :" + e,2);
				display[n] = new AccountData(acc.name,"(" + acc.type + ")");
				// display[n].account = acc.name;
				// display[n].type = acc.type;
				// display[n] = acc.type;
			}

			n++;
		}

		final String[] aName = accountName;
		final String[] aType = accountType;
		

		ArrayAdapterForAccounts adapter = new ArrayAdapterForAccounts(getActivity(), display);
		
		// REPLACE with Custom dialog and new ROW adaptor
		// see http://www.ezzylearning.com/tutorial.aspx?tid=1763429&q=customizing-android-listview-items-with-custom-arrayadapter
		
		Dialog dialog = new Dialog(getActivity());
		
		dialog.setTitle(getString(R.string.selectAccount));
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		dialog.setContentView(R.layout.dialog_account_main);
		
		ListView lv = (ListView) dialog.findViewById(R.id.accountList);
		lv.setAdapter(adapter);

		final Dialog d = dialog;
		
		lv.setOnItemClickListener(new OnItemClickListener() 
		{ 
			public void onItemClick(AdapterView<?> parent, View view, int position , long id) { 
				// accountSelection(position);
				saveAccount(aName[position],aType[position]);
				d.dismiss();
			}
		});
		
        /*
		if (aName != null)
		{
			// builder.setItems(display, new DialogInterface.OnClickListener() {
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// String item = (String) adapter.getItem(which);
					// String item = (String) adapter.getItem(which);
					Logs.myLog("Account selected = " + aName[which],1);
					saveAccount(aName[which],aType[which]);
				}
			});
		} else {
			builder.setMessage(R.string.selectAccount);
		}
		
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		
         */

        dialog.show();

	}

	private static AuthenticatorDescription getAuthenticatorDescription(String type, AuthenticatorDescription[] dictionary) {
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i].type.equals(type)) {
				return dictionary[i];
			}
		}
		// No match found
		Logs.myLog("Cannot find account description",1);
		return null;
	}
    
	protected void saveAccount(String name, String type)
	{
		// override
	}

	
}
