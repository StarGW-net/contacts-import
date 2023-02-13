package net.stargw.contactsimport;

import java.io.File;
import java.net.URLDecoder;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import static android.app.Activity.RESULT_OK;

public class FragmentBackup extends FragmentCommon  implements FragmentLifecycle, TaskCallback{

	// Mappings mappingsTools;
	Settings settings;
	
	int procFile = 0;
	FragmentBackup frag;

	private static Intent fileImport = null;
	private static Uri selectedFileURI = null;

	public FragmentBackup() {
		// TODO Auto-generated constructor stub
	}

	
	private OnClickListener buttonRecoverListener = new OnClickListener() {
		public void onClick(View v) {
			procFile = 0;
			filePicker(getActivity().getFilesDir(),".bak",false,1);
		}
	};
	
	private OnClickListener buttonBackupListener = new OnClickListener() {

		public void onClick(View v) {
			// doBackupDeleteImportPrompt(true, false, false, null, null); // no map required
			if (Global.numContacts(settings.getAccountName(),settings.getAccountType(), false) == 0)
			{
				Global.infoMessage(getActivity(),getString(R.string.warning), getString(R.string.noContactsIn) + " " + settings.getAccountName());
			} else {
				TaskController myTaskController = new TaskController();
				myTaskController.setTaskBackup(true);
				myTaskController.myTaskBackup = new TaskBackup(myTaskController,getActivity(),1,false, frag); // UTF codepage
				myTaskController.nextTask();
			}
			
		}
	};
	
	private OnClickListener buttonShareListener = new OnClickListener() {
		public void onClick(View v) {
			procFile = 1;
			filePicker(getActivity().getFilesDir(),".bak",false,0);
		}
	};
	
	
	//
	// Handler to receive messages from tasks and then update UI
	//
	public void onTaskDone(int i) 
	{
		Logs.myLog("Frgament Backup onTaskDone!",3);
		
		if (i == 3)  // loaded file
		{
			// So try and do a recover
			if ((Global.peopleRecover == null) || (Global.peopleRecover.size() == 0) )
			{
				Global.infoMessage(getActivity(),getString(R.string.failure),getString(R.string.noRecordsLoadedFromFile));
			} else {
				Logs.myLog("Loaded " + Global.peopleRecover.size() +" records",1);
				// doDeleteRecoverPrompt(true, true, null );  // no mapping
				actionPrompt();
			}
		} else {
			TextView toolsAccountName = (TextView) getView().findViewById(R.id.toolsAccountName);
			toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
					Global.numContacts(settings.getAccountName(),settings.getAccountType(),false ) ));
		}
	}
	
	
	@Override
	void processFile(File file)
	{
		String filename = file.getAbsolutePath();
				
		Logs.myLog("File selected: " + filename,2);

		Logs.myLog("Proc file: " + filename,procFile);

		if (procFile == 0)
		{
			settings.setCSVFile(filename);
			selectedFileURI = Uri.fromFile(new File(filename));
			Logs.myLog("File selected to recover: " + getContext().getFilesDir() + "/" + filename,1);
			loadRecords();
		}
		
		if (procFile == 1)
		{
			shareFile2(file);
			return;
		}
		
		if (procFile == 2)
		{
			file.delete();
			Global.infoMessage(getActivity(),getString(R.string.success),getString(R.string.deletedFile) + " " + file.getAbsolutePath());
			return;
		}
	}

	private void shareFile2(final File file) {

/*
		String fn = file.getName();

		File tempFile = new File( getContext().getCacheDir() + "/" + fn);

		Global.copyFile(file,tempFile);
		/*
		File f = new File(Global.getContext().getFilesDir(),fn);
		Logs.myLog("FILE = " + f.toString(),2);
		*/
		// getFilesDir()

		// This provides a read only content:// for other apps
		Uri uri2 = FileProvider.getUriForFile(Global.getContext(),"eu.stargw.contactsimport.fileprovider",file);

		Logs.myLog("URI PATH = " + uri2.toString(),2);

		Intent intent2 = new Intent(Intent.ACTION_SEND);
		intent2.putExtra(Intent.EXTRA_STREAM, uri2);
		intent2.setType("text/csv");
		// intent2.setType("application/octet-stream");
		intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Global.getContext().startActivity(intent2);

		/*
		File tempFile = new File( getContext().getCacheDir() + "/" + fn);

		Global.copyFile(file,tempFile);

		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/csv");
		// Uri bmpUri = Uri.fromFile(file);
		Uri bmpUri = Uri.fromFile(tempFile);
		sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
		getContext().startActivity(Intent.createChooser(sharingIntent, "Share"));
		*/
	}


		private void shareFile(final File file)
	{
		
		final Dialog dialog = new Dialog(getActivity());

		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(getResources().getString(R.string.dialogShareTitle));
		// dialog.setTitle("Share Backup File ");
		dialog.setContentView(R.layout.dialog_share);
		
		dialog.setCanceledOnTouchOutside(false);
		// dialog.setCancelable(false);

		TextView fileText = (TextView) dialog.findViewById(R.id.dialogShareFile);
		fileText.setText(file.getName());
		
		Button emailButton = (Button) dialog.findViewById(R.id.dialogShareEmailButton);

		emailButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				File tempFile = new File(getActivity().getCacheDir() , file.getName());
				dialog.dismiss();
				if (Global.copyFile(file, tempFile) == true)
				{
					Intent emailIntent = new Intent(Intent.ACTION_SEND);
					// emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					// emailIntent.setData(Uri.parse("mailto:")); // maybe..
					// emailIntent.setType("text/csv");
	
					emailIntent.setType("plain/text");

					
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, tempFile.getName());
					Uri uri = Uri.parse("file://" + tempFile);
					
					// ArrayList<Uri> u = new ArrayList<Uri>();
					// u.add(uri);
					
					emailIntent.setType("application/octet-stream");
					emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
					try {
						getActivity().startActivity(emailIntent);
					} catch (Exception e) {
						Logs.myLog("Email backup failed! "+ e,1);
						Global.infoMessage(getActivity(),getString(R.string.warning),getString(R.string.emailFailed));
					}
				} else {
					Global.infoMessage(getActivity(),getString(R.string.failure),getString(R.string.emailFailed));
				}
			}
		});
		
		Button copyButton = (Button) dialog.findViewById(R.id.dialogShareCopyButton);

		copyButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				// File tempFile = new File(getActivity().getCacheDir() , file.getName());

				File newFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.contactsFilesDir));
				if (!newFolder.exists()) {
					newFolder.mkdir();
				}
				
				File tempFile = new File(newFolder, file.getName());
				
				dialog.dismiss(); // info dialog!!
				if (Global.copyFile(file, tempFile) == true)
				{
					Global.infoMessage(getActivity(),getString(R.string.success),getString(R.string.backupFile) + " " + tempFile.getAbsolutePath());
				} else {
					Global.infoMessage(getActivity(),getString(R.string.failure),getString(R.string.backupFileFailed));
				}
			}
		});
		/*
		final TextView cp = (TextView) dialog.findViewById(R.id.dialogShareCodePagePicker);
		codePagePicker(cp);
		 */	
		
		dialog.show();
	
	}




	
	private void loadRecords()
	{
		// use diff data structure? 
		// Does people need to be global? Used in import only?

		// Global.peopleRecover = LoadCSV.getRecords(filename,"UTF-8"); // supply filename?
		
		// Free memory
		Global.peopleRecover = null;
		Global.peopleImport = null;
		System.gc();
		
		TaskController myTaskController = new TaskController();
		
		myTaskController.setTaskLoadCSV(true);
		myTaskController.myTaskLoadCSV = new TaskLoadCSV(myTaskController,getActivity(),selectedFileURI,"UTF-8", this, false);
		myTaskController.nextTask();
	}
	
	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_backup, container, false);
		return view;
	}
	
	/** Called when the activity is first created. */
	@Override
	// public void onCreate(Bundle savedInstanceState) {
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// getView().setContentView(R.layout.activity_tools);

		Logs.myLog("Fragment Backup Created",3);
		
		frag = this;
		
		// Button buttonDelete = (Button) getView().findViewById(R.id.toolsDeleteButton);
		// buttonDelete.setOnClickListener(buttonDeleteListener); 
		
		Button buttonRecover = (Button) getView().findViewById(R.id.toolsRecoverButton);
		buttonRecover.setOnClickListener(buttonRecoverListener); 
		
		Button buttonBackup = (Button) getView().findViewById(R.id.toolsBackupButton);
		buttonBackup.setOnClickListener(buttonBackupListener); 
		
		Button shareBackup = (Button) getView().findViewById(R.id.toolsShareButton);
		shareBackup.setOnClickListener(buttonShareListener); 
		
		settings = new Settings();
		
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.toolsAccountName);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}



	
	private void actionPrompt()
	{
		
		final int num = Global.numContacts(Global.accountName, Global.accountType, true);
		
		if (num > 0)
		{
			Dialog dialog = new Dialog(getActivity());
			
			dialog.setContentView(R.layout.dialog_action_prompt);
			dialog.setTitle(getString(R.string.warning));
			
			TextView text1 = (TextView) dialog.findViewById(R.id.dialogActionText);
		
			text1.setText(String.format(getString(R.string.dialogAction03), num, Global.accountName, (Global.peopleRecover.size() - 1) ));
			
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
					CheckBox checkBox = (CheckBox) fdialog.findViewById(R.id.dialogActionCB);
					boolean backup = checkBox.isChecked();
					fdialog.dismiss();
					actionExecute(backup);
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

	void actionExecute(boolean backup)
	{
		TaskController myTaskController = new TaskController();
		if (backup == true)
		{
			myTaskController.setTaskBackup(true);
			myTaskController.myTaskBackup = new TaskBackup(myTaskController,getActivity(),1,false, this); // UTF codepage
		}
		
		int num = Global.numContacts(Global.accountName, Global.accountType, true);
		
		if (num > 0)
		{
			myTaskController.setTaskDelete(true);
			myTaskController.myTaskDelete = new TaskDeleteAll(myTaskController,getActivity(), this );
		}

		myTaskController.setTaskRecover(true);
		myTaskController.myTaskRecover = new TaskRecover(myTaskController,getActivity(), this);

		myTaskController.nextTask();
	}


	@Override
	public void onResume()
	{
		super.onResume();

		Logs.myLog("Fragment Backup onResume!",3);
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.toolsAccountName);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));

		if (fileImport != null) {
			Logs.myLog("Need to import the CSV file now...",3);
			processIntent();
		}
	}

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

				if (fileExtension.equalsIgnoreCase("bak")) {
					String filename = myFile.substring(myFile.lastIndexOf('/') + 1);

					String f2;
					try {
						f2 = URLDecoder.decode(filename, "UTF-8");
					} catch (Exception e) {
						f2 = filename;
					}

					Logs.myLog("file selected: " + f2, 2);

					// settings.setCSVFile(f2);
					// settings.save("import");

					// STEVE - do the load from here now...
					settings.setCSVFile(filename);

					fileImport = data;  // on resume is called after the picker returns

					selectedFileURI = fileImport.getData();
				} else {
					Global.infoMessage(getContext(),"File Error","File not a .bak file produced by this app. Use Import instead!");
				}

			}
		}
	}

	//
	// Process the file selected by the file picker
	//
	void processIntent() {
		// Do the load here...
		fileImport = null; // reset so onResume will not kick this off again!
		loadRecords();
	}


	public void onRefreshFragment() {
		// TODO Auto-generated method stub
		Logs.myLog("Frgament Backup onRefresh!",3);
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.toolsAccountName);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}

}
