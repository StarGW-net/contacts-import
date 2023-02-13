package net.stargw.contactsimport;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FragmentView extends FragmentCommon implements FragmentLifecycle {

	Settings settings;
	
	
	private OnClickListener buttonViewGroupsListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent intent = new Intent(getActivity(), ActivityViewContacts.class);
			// intent.putExtra("action","viewGroups");
			// startActivity(intent);
			DialogViewGroups viewGroups = new DialogViewGroups();
			viewGroups.onCreate(getActivity(),"viewGroups");
		}
	};
	
	private OnClickListener buttonViewContactsListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent intent = new Intent(getActivity(), ActivityViewContacts.class);
			// intent.putExtra("action","viewContacts");
			// startActivity(intent);
			DialogViewContacts viewContacts = new DialogViewContacts();
			viewContacts.onCreate(getActivity(),2, (long) 0);
		}
	};
	
	
	private OnClickListener buttonAccountsListener = new OnClickListener() {
		public void onClick(View v) {
			accountPicker();
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_view, container, false);
		return view;
	}
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setContentView(R.layout.activity_export);

		Logs.myLog("Fragment View Created",3);
		
		Button buttonAccounts = (Button) getView().findViewById(R.id.viewSelectAccountButton);
		buttonAccounts.setOnClickListener(buttonAccountsListener); 
		
		Button buttonLoad = (Button) getView().findViewById(R.id.viewGroupsButton);
		buttonLoad.setOnClickListener(buttonViewGroupsListener); 
		
		Button buttonMapping = (Button) getView().findViewById(R.id.viewContactsButton);
		buttonMapping.setOnClickListener(buttonViewContactsListener); 
		
		
		settings = new Settings();
		settings.load("view");
		
	
		TextView text = (TextView) getView().findViewById(R.id.viewSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));

	}

	@Override
	protected void saveAccount(String name, String type)
	{
		settings.setAccountName(name);
		settings.setAccountType(type);
		TextView text = (TextView) getView().findViewById(R.id.viewSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
		settings.save("view");
	}

	@Override
	public void onResume()
	{
		super.onResume();
	
		Logs.myLog("Fragment View onResume!",3);
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.viewSelectAccountText);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}


	public void onRefreshFragment() {
		Logs.myLog("Fragment View onRefresh!",3);
		
		// TODO Auto-generated method stub
		TextView text = (TextView) getView().findViewById(R.id.viewSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}
	
}

