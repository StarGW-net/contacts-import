package net.stargw.contactsimport;

import java.io.File;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

public class FragmentHelp extends FragmentCommon implements FragmentLifecycle { 

	public FragmentHelp() {
		// TODO Auto-generated constructor stub
	}

	private OnClickListener buttonViewLogsListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent intent = new Intent(getActivity(),ActivityLogs.class);
			// startActivity(intent);
			DialogViewLogs DialogViewLogs = new DialogViewLogs();
			DialogViewLogs.onCreate(getActivity());
		}
	};
	

	private OnClickListener buttonViewHelpListener = new OnClickListener() {
		public void onClick(View v) {
			showHelp();
		}
	};
	
	private OnClickListener buttonEmailDeveloperListener = new OnClickListener() {
		public void onClick(View v) {
			shareLog();
		}
	};
	
	
	private OnClickListener buttonBuyProListener = new OnClickListener() {
		public void onClick(View v) {
			final String appPackageName = "eu.stargw.contactsimport"; 
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help, container, false);
		return view;
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
		Context context = getActivity();
		
	
		Button buttonViewLogs = (Button) getView().findViewById(R.id.settingsLogsButton);
		buttonViewLogs.setOnClickListener(buttonViewLogsListener); 
	
		Button buttonViewHelp = (Button) getView().findViewById(R.id.settingsHelpButton);
		buttonViewHelp.setOnClickListener(buttonViewHelpListener); 
		
		Button buttonEmailDeveloper = (Button) getView().findViewById(R.id.settingsEmailDeveloperButton);
		buttonEmailDeveloper.setOnClickListener(buttonEmailDeveloperListener); 
		
		Button buttonBuyPro = (Button) getView().findViewById(R.id.settingsBuyProButton);


		if (Global.maxContacts > 50)
		{
			buttonBuyPro.setVisibility(View.GONE);
		} else {
			buttonBuyPro.setOnClickListener(buttonBuyProListener); 
		}

	}

	private void emailDeveloper()
	{

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("plain/text");

		
		// emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// emailIntent.setData(Uri.parse("mailto: contactsimport@stargw.eu")); // maybe..
		
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "contactsimport@stargw.net" });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.developerEmailSubject));


		
		// Log file as attachment
		File tempFile = Logs.copyLogFile();
		if (tempFile != null)
		{
			// emailIntent.setType("text/plain");
			Uri uri = Uri.parse("file://" + tempFile.getAbsolutePath());

			// ArrayList<Uri> u = new ArrayList<Uri>();
			// u.add(uri);
			
			// emailIntent.putExtra(Intent.EXTRA_STREAM, u);
			
			// emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, u);
			emailIntent.setType("application/octet-stream");
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}


		
		try {
			getActivity().startActivity(emailIntent);
		} catch (Exception e) {
			Logs.myLog("Email to developer contactsimport@stargw.net failed! " + e,1);
			Global.infoMessage(getActivity(),getString(R.string.warning),getString(R.string.emailFailed));
		}


	}

	// share log
	public static void shareLog()
	{
		File f = Logs.getLogFile();

		Uri uri2 = FileProvider.getUriForFile(Global.getContext(),"eu.stargw.contactsimport.fileprovider",f);

		Logs.myLog("URI PATH = " + uri2.toString(),2);

		Intent intent2 = new Intent(Intent.ACTION_SEND);
		intent2.putExtra(Intent.EXTRA_STREAM, uri2);
		intent2.setType("text/plain");
		// intent2.setType("application/octet-stream");
		intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Global.getContext().startActivity(intent2);

	}

	// share log
	public static void shareLogOLD()
	{

		File tempFile = Logs.copyLogFile();


		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		// Uri bmpUri = Uri.fromFile(file);
		Uri bmpUri = Uri.fromFile(tempFile);
		sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
		sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent chooserIntent = Intent.createChooser(sharingIntent, "Share");
		chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Global.getContext().startActivity(Intent.createChooser(sharingIntent, "Share"));
		Global.getContext().startActivity(chooserIntent);

	}


	private void emailGraphics()
	{

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// emailIntent.setData(Uri.parse("mailto: info@fluffestudio.com")); // maybe..
		
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "info@fluffestudio.com" });
		emailIntent.setType("plain/text");
		//emailIntent.setType("message/rfc822");
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.developerEmailSubject));
		try {
			getActivity().startActivity(emailIntent);
		} catch (Exception e) {
			Logs.myLog("Email to graphics designed failed! " + e,1);
			Global.infoMessage(getActivity(),getString(R.string.warning),getString(R.string.emailFailed));
		}


	}

	//
	// Display the help screen
	//
	private void showHelp()
	{

		String verName = "latest";
		try {

			PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			verName = pInfo.versionName;

		} catch (PackageManager.NameNotFoundException e) {
			Logs.myLog("Could not get version number", 3);
		}

		String url = "https://www.stargw.net/apps/contactsimport/help.html?ver=" + verName;
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);

	}

	//
	// Display the help screen
	//
	private void showHelp2()
	{
		
		final Dialog help = new Dialog(getActivity());
		
		help.setTitle(getResources().getString(R.string.version));

		help.setContentView(R.layout.dialog_help);
		
		help.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		
		Button dialogButton = (Button) help.findViewById(R.id.dialogHelpCancel);
		// if button is clicked, close the custom dialog

		
		dialogButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				help.cancel();
			}
		});

		TextView textEmail = (TextView) help.findViewById(R.id.dialogHelpTextGraphicsEmail);
		
		textEmail.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				emailGraphics();
			}
		});
		
		ImageView iconLogo = (ImageView) help.findViewById(R.id.iconLogo);
		
		iconLogo.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				emailGraphics();
			}
		});
		
		help.show();
	}

	public void onPauseFragment() {
		// TODO Auto-generated method stub
		
	}

	public void onResumeFragment() {
		// TODO Auto-generated method stub
		
	}

	public void onRefreshFragment() {
		// TODO Auto-generated method stub
		
	}
}
