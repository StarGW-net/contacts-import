package net.stargw.contactsimport;

import java.io.File;
import java.net.URLDecoder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_MIME_TYPES;

public class FragmentImport extends FragmentCommon implements FragmentLifecycle, TaskCallback {

	Mappings mappingsImport;
	Settings settings;
	
	Context context;

	private static Intent fileImport = null;
	private static Uri selectedFileURI = null;

	//
	// Button to start import 
	//
	private OnClickListener buttonImportListener = new OnClickListener() {
		public void onClick(View v) {
			
			mappingsImport.load(); // in case they have change

			if (preCheckOK())
			{
				actionPrompt();
			}
		}
	};

	

	
	public void onTaskDone(int i) 
	{
		// TODO Auto-generated method stub
		TextView text = (TextView) getView().findViewById(R.id.importFileText);
		Button buttonLoad = (Button) getView().findViewById(R.id.importImportButton);
		
		TextView importAccountName = (TextView) getView().findViewById(R.id.importAccountName);
		if ((i == 4) || (i == 3))
		{ 
			// Use cached value
			importAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
		} else {
			importAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
					Global.numContacts(settings.getAccountName(),settings.getAccountType(), false ) ));
		}
		TextView buttonMapText = (TextView) getView().findViewById(R.id.importMapText);
		buttonMapText.setText(getString(R.string.text03) + mappingsImport.getProfileText());
		
		String fileName = settings.getCSVFile();
		File file = new File(fileName);
		
		if ((Global.peopleImport != null) && (Global.peopleImport.size() != 0) )
		{
			buttonLoad.setEnabled(true);
			
			text.setText(String.format(getString(R.string.text04), file.getName(), Global.peopleImport.size()));

			if ( i == 3) // file load
			{
				if (checkOurBackup() == true)
				{
					Logs.myLog("Import was created by this app",2);
				} else {
					Logs.myLog("Foreign Import",2);
				}
	
				
				if (Global.peopleImport.size() > Global.maxContacts)
				{
					if (Global.maxContacts > 50)
					{
						Global.infoMessage(context,getString(R.string.warning),String.format(getString(R.string.maxContacts), Global.maxContacts));
					} else {
						Global.infoMessage(context,getString(R.string.warning),String.format(getString(R.string.upgrade), Global.maxContacts, Global.maxContacts));
					}
				}
			}
		} else {
			if ( i == 3 ) // file load
			{
				// Failed to load CSV file
				Global.infoMessage(context,getString(R.string.warning),getString(R.string.error01) + fileName);
			} 
			
			buttonLoad.setEnabled(false);
			text.setText(getString(R.string.text02));
		}
	}
	
	
	//
	// Decide the field mappings
	//
	private OnClickListener buttonMappingListener = new OnClickListener() {
		public void onClick(View v) {
			showMappings();
		}
	};
	
	
	//
	// Pick and load a new CSV file
	//
	private OnClickListener buttonCSVListenerX = new OnClickListener() {
		public void onClick(View v) {
			filePicker(Environment.getExternalStorageDirectory(),".csv",true,0);
		}
		

	};

	private OnClickListener buttonCSVListener = new OnClickListener() {
		public void onClick(View v) {

			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());

			Intent intentCSV = new Intent(Intent.ACTION_GET_CONTENT);
			// Intent intentCSV = new Intent(Intent.ACTION_OPEN_DOCUMENT);

			intentCSV.setType("text/*");
			// intentCSV.setType("*/*");
			// intentCSV.setType("file/weight*.csv");

			String[] mimetypes = {"text/comma-separated-values", "text/csv"};
			intentCSV.putExtra(EXTRA_MIME_TYPES, mimetypes);

			intentCSV.addCategory(Intent.CATEGORY_OPENABLE);

			Intent chooserIntent = Intent.createChooser(intentCSV, "Import CSV");

			try {
				startActivityForResult(chooserIntent, 1);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(context.getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
			}

		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {
			Logs.myLog("Result Code = " + resultCode, 2);
			if (resultCode == RESULT_OK) {
				String myFile = data.getData().toString();
				Logs.myLog("Got = " + myFile, 2);

				String fileExtension = MimeTypeMap.getFileExtensionFromUrl(myFile);
				Logs.myLog("Got = " + fileExtension, 2);

				String filename = myFile.substring(myFile.lastIndexOf('/') + 1);

				String f2;
				try {
					f2 = URLDecoder.decode(filename, "UTF-8");
				} catch (Exception e) {
					f2 = filename;
				}

				Logs.myLog("file selected: " + f2,2);

				settings.setCSVFile(f2);
				settings.save("import");

				fileImport = data;  // on resume is called after the pciker returns

			}
		}
	}

	//
	// Process the file selected by the file picker
	//
	void processIntent()
	{
		String filename = settings.getCSVFile();

		Logs.myLog("file selected: " + filename,2);

		final Dialog dialog = new Dialog(getActivity());

		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(getResources().getString(R.string.dialogImportTitle));
		// dialog.setTitle("Share Backup File ");
		dialog.setContentView(R.layout.dialog_import_codepage);

		dialog.setCanceledOnTouchOutside(false);
		// dialog.setCancelable(false);

		TextView fileText = (TextView) dialog.findViewById(R.id.dialogImportFile);
		fileText.setText(filename);

		Button button = (Button) dialog.findViewById(R.id.dialogImportButton);

		selectedFileURI = fileImport.getData();
		fileImport = null; // reset so onResume will not kick this off again!

		button.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				dialog.dismiss();
				// loadRecords();
				// Now what ?????
				/*
				Thread thread = new Thread() {
					@Override
					public void run() {
						importFile(fileImport);
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(Global.IMPORT_DONE);
						myContext.sendBroadcast(broadcastIntent);
					}
				};

				OR
								 */
				loadRecords2();



			}
		});

		final TextView cp = (TextView) dialog.findViewById(R.id.dialogImportCodePagePicker);
		codePagePicker(cp);


		dialog.show();

	}

	//
	// Process the file selected by the file picker
	//
	@Override
	void processFile(File file)
	{
		String filename = file.getAbsolutePath();
				
		Logs.myLog("file selected: " + filename,2);
		
		settings.setCSVFile(filename);
		settings.save("import");



		final Dialog dialog = new Dialog(getActivity());

		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(getResources().getString(R.string.dialogImportTitle));
		// dialog.setTitle("Share Backup File ");
		dialog.setContentView(R.layout.dialog_import_codepage);
		
		dialog.setCanceledOnTouchOutside(false);
		// dialog.setCancelable(false);

		TextView fileText = (TextView) dialog.findViewById(R.id.dialogImportFile);
		fileText.setText(file.getName());
		
		Button button = (Button) dialog.findViewById(R.id.dialogImportButton);
		
		button.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				dialog.dismiss();

				loadRecords();
			}
		});
		
		final TextView cp = (TextView) dialog.findViewById(R.id.dialogImportCodePagePicker);
		codePagePicker(cp);
		
		
		dialog.show();

	}
	
	@Override
	protected void codePageSave(int i)
	{
		// stub to override
		settings.setCodePage(i);
		settings.save("import");
	}
	
	@Override
	protected int codePageLoad()
	{
		return settings.getCodePage();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_import, container, false);
		return view;
	}
	

	

	@Override
	public void onResume()
	{
		super.onResume();

		Logs.myLog("Fragment Import onResume!",3);
		onTaskDone(4);

		if (fileImport != null) {
			Logs.myLog("Need to import the CSV file now...",3);
			processIntent();
		}

	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setContentView(R.layout.activity_import);

		Logs.myLog("Fragment Import Created",3);
		
		// Capture our button from layout
		Button buttonLoad = (Button) getView().findViewById(R.id.importImportButton);
		
		// Register the onClick listener with the implementation above
		buttonLoad.setOnClickListener(buttonImportListener); 
		
		Button buttonCSV = (Button) getView().findViewById(R.id.importCSVButton);
		buttonCSV.setOnClickListener(buttonCSVListener); 
		
		Button buttonMapping = (Button) getView().findViewById(R.id.importMappingsButton);
		buttonMapping.setOnClickListener(buttonMappingListener); 
		
		context = getActivity();
		
		mappingsImport = new Mappings();
		mappingsImport.load();
		
		settings = new Settings();
		settings.load("import");
		
		/*
		TextView buttonMapText = (TextView) getView().findViewById(R.id.importMapText);
		buttonMapText.setText(getString(R.string.text03) + mappingsImport.getProfileText());
		
		TextView importAccountName = (TextView) getView().findViewById(R.id.importAccountName);
		importAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.checkContacts(settings.getAccountName(),settings.getAccountType() ) ));
		*/
		
		onTaskDone(4); // refresh GUI on start/restart etc.

		
	}

	
	
	private void showMappings()
	{
		// Intent intent = new Intent(getActivity(), ActivityMappingsImport.class);
		// startActivity(intent);
		DialogMappingsImport mappings = new DialogMappingsImport();
		mappings.onCreate(getActivity(),this);
	}
	

	private void loadRecords()
	{

		// Free memory
		Global.peopleRecover = null;
		Global.peopleImport = null;
		System.gc();
		
		// String fileName = settings.getCSVFile();
		
		TaskController myTaskController = new TaskController();
		
		myTaskController.setTaskLoadCSV(true);
		myTaskController.myTaskLoadCSV = new TaskLoadCSV(myTaskController,context,selectedFileURI,settings.getCodePageText(), this, true);
		myTaskController.nextTask();
		
		// Global.people = LoadCSV.getRecords(fileName,mappingsImport.getCodePageText());


	}

	private void loadRecords2()
	{

		// Free memory
		Global.peopleRecover = null;
		Global.peopleImport = null;
		System.gc();

		String fileName = settings.getCSVFile();

		Global.iPath = selectedFileURI.getPath();

		TaskController myTaskController = new TaskController();

		myTaskController.setTaskLoadCSV(true);
		myTaskController.myTaskLoadCSV = new TaskLoadCSV(myTaskController,context,selectedFileURI,settings.getCodePageText(), this, true);
		myTaskController.nextTask();

		// Global.people = LoadCSV.getRecords(fileName,mappingsImport.getCodePageText());


	}

	private boolean checkOurBackup()
	{
		// Iterator<String[]> iterator = Global.peopleImport.iterator();
		// String[] contactRecord = iterator.next();
		
		String[] contactRecord = Global.peopleImport.get(0);
		
		// String[] contact = contactRecord.getmText();
		
		
		for (int i = 0; i < contactRecord.length; i++ )
		{
			// int n = contact[i]; // need a reverse name to int here!!
			if (contactRecord[i] != null)
			{
				boolean matched = false;
				for (int k = 0; k < Global.getContactFieldNum(); k++)
				{
	
					if (contactRecord[i].equals(Global.getContactField(k)))
					{
						matched = true;
						continue;
					}
				}
				if (matched == false)
				{
					return false; // No match so cannot be ours!
				}
			}
		}
		
		mappingsImport.defaultPrefs(); // blank all fields
		
		mappingsImport.setConcatAddress(true);
		mappingsImport.setIgnoreFirstLine(1);
		
		mappingsImport.setProfile(1); // custom
		
		// populate mappings and then call generic add contacts
		
		// Iterator<String[]> iterator2 = Global.peopleImport.iterator();
		// String[] contactRecord2 = iterator2.next();
		
	
		// Construct the mappings based on the contents of the first row
		for (int i = 0; i < contactRecord.length; i++ )
		{
			// int n = contact[i]; // need a reverse name to int here!!
			for (int k = 0; k < Global.getContactFieldNum(); k++)
			{
				if (contactRecord[i].equals(Global.getContactField(k)))
				{
					mappingsImport.setField(i,k);
					continue;
				}
			}
		}
		
		TextView buttonMapText = (TextView) getView().findViewById(R.id.importMapText);
		buttonMapText.setText(getString(R.string.text03) + mappingsImport.getProfileText());
		
		mappingsImport.save();
		return true;

	}
	
	private boolean preCheckOK()
	{
		if (mappingsImport.getProfile() != 0)
		{
			return true;
		}
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		 
		// set title
		alertDialogBuilder.setTitle(getString(R.string.warning));

		alertDialogBuilder.setMessage(getString(R.string.text05));

		alertDialogBuilder.setCancelable(false);
		

		alertDialogBuilder.setPositiveButton(getString(android.R.string.ok),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					actionPrompt();
				}
			});
	

		alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

		
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
 
		// show it
		alertDialog.show();
		
		return false;
	}
	
	private void actionPrompt()
	{
		
		final int num = Global.numContacts(Global.accountName, Global.accountType, true);
		
		if (num > 0)
		{
			Dialog dialog = new Dialog(context);
			
			dialog.setContentView(R.layout.dialog_action_prompt);
			dialog.setTitle(getString(R.string.warning));
			
			TextView text1 = (TextView) dialog.findViewById(R.id.dialogActionText);
			/*
			text1.setText("Delete " + num + " contacts from account \"" + Global.accountName + "\"" 
					+ " and import " + (Global.peopleImport.size() - mappingsImport.getIgnoreFirstLine())
					+ " contacts from file:");
			*/
			
			text1.setText(String.format(getString(R.string.dialogAction02), num, Global.accountName, (Global.peopleImport.size() - mappingsImport.getIgnoreFirstLine())));
			
			TextView text2 = (TextView) dialog.findViewById(R.id.dialogActionFile);
			text2.setText(settings.getCSVFile());
			
			TextView text3 = (TextView) dialog.findViewById(R.id.dialogActionCBText);
			text3.setText(getString(R.string.dialogAction01));
			

			// LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.dialogActionDeleteOption);
			// layout.setVisibility(View.GONE);
			
			
			Button dialogButtonOK = (Button) dialog.findViewById(R.id.dialogActionOK);
	
			final Dialog fdialog = dialog;
			
			dialogButtonOK.setOnClickListener(new OnClickListener() {
				// @Override
				public void onClick(View v) {
					fdialog.dismiss();
					CheckBox checkBox = (CheckBox) fdialog.findViewById(R.id.dialogActionCB);
					actionExecute(checkBox.isChecked());
				}
			});
			
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogActionCancel);
			
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
				// @Override
				public void onClick(View v) {
					fdialog.dismiss();
				}
			});
	
			
			dialog.show();
		} else {
			actionExecute(false);
		}
	}

	void actionExecute(boolean delete)
	{
		mappingsImport.save();
		
		TaskController myTaskController = new TaskController();
		if (delete == true)
		{
			myTaskController.setTaskBackup(true);
			myTaskController.myTaskBackup = new TaskBackup(myTaskController,getActivity(),1,false, null); // UTF codepage
		}
		
		int num = Global.numContacts(Global.accountName, Global.accountType, true);
		if (num > 0)
		{
			myTaskController.setTaskDelete(true);
			myTaskController.myTaskDelete = new TaskDeleteAll(myTaskController,getActivity(), this);
		}

		myTaskController.setTaskImport(true);
		myTaskController.myTaskImport = new TaskImport(myTaskController,getActivity(), this);

		myTaskController.nextTask();
	}


	public void onRefreshFragment() {
		// TODO Auto-generated method stub
		Logs.myLog("Frgament Import onRefresh!",3);
		TextView importAccountName = (TextView) getView().findViewById(R.id.importAccountName);
		importAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(),true ) ));
		
		mappingsImport.load();
		TextView buttonMapText = (TextView) getView().findViewById(R.id.importMapText);
		buttonMapText.setText(getString(R.string.text03) + mappingsImport.getProfileText());
	}


}

