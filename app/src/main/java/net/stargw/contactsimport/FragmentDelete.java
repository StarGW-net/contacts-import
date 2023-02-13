package net.stargw.contactsimport;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FragmentDelete extends FragmentCommon  implements FragmentLifecycle, TaskCallback {

	Settings settings;
	FragmentDelete me;
	
	private OnClickListener buttonDeleteGroupsListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent intent = new Intent(getActivity(), ActivityDeleteGroups.class);
			// intent.putExtra("action","deleteGroups");
			// startActivity(intent);
			DialogDeleteGroups delGroup = new DialogDeleteGroups();
			delGroup.onCreate(getActivity(),me);
		}
	};
	
	private OnClickListener buttonDeleteContactsListener = new OnClickListener() {
		public void onClick(View v) {
			// Intent intent = new Intent(getActivity(), ActivityDeleteContacts.class);
			// intent.putExtra("action","deleteContacts");
			// startActivity(intent);
			DialogDeleteContacts delContacts = new DialogDeleteContacts();
			delContacts.onCreate(getActivity(),me);
		}
	};
	
	
	private OnClickListener buttonAccountsListener = new OnClickListener() {
		public void onClick(View v) {
			accountPicker();
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_delete, container, false);
		return view;
	}
	
	
	/** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setContentView(R.layout.activity_export);

		Logs.myLog("Fragment Delete Created",3);
		
		Button buttonAccounts = (Button) getView().findViewById(R.id.deleteSelectAccountButton);
		buttonAccounts.setOnClickListener(buttonAccountsListener); 
		
		Button buttonLoad = (Button) getView().findViewById(R.id.deleteGroupsButton);
		buttonLoad.setOnClickListener(buttonDeleteGroupsListener); 
		
		Button buttonMapping = (Button) getView().findViewById(R.id.deleteContactsButton);
		buttonMapping.setOnClickListener(buttonDeleteContactsListener); 
		
		
		settings = new Settings();
		settings.load("del");
		
		me = this;
	
		TextView text = (TextView) getView().findViewById(R.id.deleteSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));

	}

	@Override
	protected void saveAccount(String name, String type)
	{
		settings.setAccountName(name);
		settings.setAccountType(type);
		TextView text = (TextView) getView().findViewById(R.id.deleteSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
		settings.save("del");
	}

	@Override
	public void onResume()
	{
		super.onResume();
	
		Logs.myLog("Fragment Delete onResume!",3);
		TextView toolsAccountName = (TextView) getView().findViewById(R.id.deleteSelectAccountText);
		toolsAccountName.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}

	public void onRefreshFragment() {
		// TODO Auto-generated method stub
		Logs.myLog("Frgament Delete onRefresh!",3);
		TextView text = (TextView) getView().findViewById(R.id.deleteSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), true ) ));
	}


	public void onTaskDone(int i) {
		// TODO Auto-generated method stub
		Logs.myLog("Frgament Delete onTaskDone!",3);
		TextView text = (TextView) getView().findViewById(R.id.deleteSelectAccountText);
		text.setText(String.format(getString(R.string.currentAccount02),settings.getAccountName(),
				Global.numContacts(settings.getAccountName(),settings.getAccountType(), false ) ));
	}
	
}

