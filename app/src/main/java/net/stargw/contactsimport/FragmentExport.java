package net.stargw.contactsimport;


import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FragmentExport extends FragmentCommon implements FragmentLifecycle {

	// Mappings mappingsExport;
	Settings settings;
	
	Context context;
	
	private OnClickListener buttonExportContactsListener = new OnClickListener() {
		public void onClick(View v) {
			
			if (Global.numContacts(settings.getAccountName(),settings.getAccountType(), true) == 0)
			{
				Global.infoMessage(context,getString(R.string.warning), getString(R.string.noContactsIn) + " " + settings.getAccountName());
			} else {
				// task export
				final Dialog dialog = new Dialog(getActivity());
	
				// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setTitle(getResources().getString(R.string.dialogExportTitle));
	
				dialog.setContentView(R.layout.dialog_export_codepage);
				
				dialog.setCanceledOnTouchOutside(false);
	
				
				Button button = (Button) dialog.findViewById(R.id.dialogExportButton);
				
				button.setOnClickListener(new OnClickListener() {
					// @Override
					public void onClick(View v) {
						dialog.dismiss();
						startExport();
					}
				});
				
				final TextView cp = (TextView) dialog.findViewById(R.id.dialogExportCodePagePicker);
				codePagePicker(cp);
				
				dialog.show();
			}
		}
	};	
/*
	private OnClickListener buttonExportShareListener = new OnClickListener() {
		public void onClick(View v) {
			File folder = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.contactsFilesDir));
			filePicker(folder,".csv",false,0);
		}
	};
	
	@Override
	void processFile(File file)
	{
		String filename = file.getAbsolutePath();
				
		Logs.myLog("File selected: " + filename,1);
		
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// emailIntent.setData(Uri.parse("mailto:"));
		// emailIntent.setType("text/csv");
		emailIntent.setType("plain/text");

		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
		Uri uri = Uri.parse("file://" + file);
		
		// ArrayList<Uri> u = new ArrayList<Uri>();
		// u.add(uri);
		emailIntent.setType("application/octet-stream");
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		try {
			getActivity().startActivity(emailIntent);
		} catch (Exception e) {
			Logs.myLog("Email export failed! " + e,1);
			Global.infoMessage(getActivity(),getString(R.string.warning),getString(R.string.emailFailed));
		}
	}
*/
	private void startExport()
	{
		settings.save("export");

		TaskController myTaskController = new TaskController();
		myTaskController.setTaskExport(true);
		myTaskController.myTaskExport = new TaskExport(myTaskController,context);
		myTaskController.nextTask();

		/*
		if (settings.getAccountName().equals(Global.accountName))
		{
			TaskController myTaskController = new TaskController();
			myTaskController.setTaskBackup(true);
			myTaskController.myTaskBackup = new TaskBackup(myTaskController,context,settings.getCodePage(),true, null);
			myTaskController.nextTask();
		} else {
			TaskController myTaskController = new TaskController();
			myTaskController.setTaskExport(true);
			myTaskController.myTaskExport = new TaskExport(myTaskController,context);
			myTaskController.nextTask();
		}
		*/
	}

	
	@Override
	protected void codePageSave(int i)
	{
		// stub to override
		settings.setCodePage(i);
		// mappingsExport.savePref();
	}
	
	@Override
	protected int codePageLoad()
	{
		return settings.getCodePage();
	}
	
	
	private OnClickListener buttonAccountsListener = new OnClickListener() {
		public void onClick(View v) {
			accountPicker();
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_export, container, false);
		return view;
	}


	
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setContentView(R.layout.activity_export);

		Logs.myLog("Fragment Export Created",3);
		
		Button buttonAccounts = (Button) getView().findViewById(R.id.exportSelectAccountButton);
		buttonAccounts.setOnClickListener(buttonAccountsListener); 
		
		Button buttonLoad = (Button) getView().findViewById(R.id.exportContactsButton);
		buttonLoad.setOnClickListener(buttonExportContactsListener); 

		/*
		Button buttonShare = (Button) getView().findViewById(R.id.exportShareButton);
		buttonShare.setOnClickListener(buttonExportShareListener); 
		*/

		context = getActivity();
		
		settings = new Settings();
		settings.load("export");
		
		// mappingsImport.loadPref(); - already done?
		TextView text = (TextView) getView().findViewById(R.id.exportSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}

	
	@Override
	protected void saveAccount(String name, String type)
	{
		settings.setAccountName(name);
		settings.setAccountType(type);
		TextView text = (TextView) getView().findViewById(R.id.exportSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) )); // even use cached value if available!
		settings.save("export");
	}

	@Override
	public void onResume()
	{
		super.onResume();
	
		Logs.myLog("Fragment Export onResume!",3);
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.exportSelectAccountText);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}

	public void onRefreshFragment() {
		// TODO Auto-generated method stub
		Logs.myLog("Frgament Export onRefresh!",3);
		TextView text = (TextView) getView().findViewById(R.id.exportSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}

	
}

