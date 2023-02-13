package net.stargw.contactsimport;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DialogViewGroups implements TaskCallback {

	Settings settings;
	String action;
	
	Dialog dialog;
	Context context;


	// Global variables :-)
	private ListView lv;
	private List<ContactRecordView> groups = new ArrayList<ContactRecordView>();

	int state = 0; // List ALL Groups
	



	/** Called when the activity is first created. */

	public void onCreate(Context c, String action) {
		
		context = c;
		dialog = new Dialog(context,R.style.SWDialog);
		// dialog = new Dialog(context,R.style.dialog_full_screen);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		dialog.setContentView(R.layout.dialog_contacts_main);
		
		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

		// Get the ListView
		lv = (ListView) dialog.findViewById(R.id.contactList);
		
		lv.setOnItemClickListener(new OnItemClickListener() 
		{ 
			public void onItemClick(AdapterView<?> parent, View view, int position , long id) { 
				Selection(position);
			}
		});
		
		
		settings = new Settings();
		settings.load("view");

		dialog.show();
		
		// Start with a Group view of ALL groups
		state = 0;
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskGetContactsView(true);
		myTaskController.myTaskGetContactsView = new TaskGetContactsView(myTaskController,context,groups, this, settings, state, (long) 0);
		myTaskController.nextTask();
		
	}


	//
	// Handle when an item is selected from the ListView
	//
	private void Selection(int position)
	{
		long n = 0;

		n = groups.get(position).getId();
		if (n == 0)
		{
			state = 2;
		} else {
			state = 1;
		}
		DialogViewContacts viewContacts = new DialogViewContacts();
		viewContacts.onCreate(context,state, n);

	}


	public void onTaskDone(int i) {
		// TODO Auto-generated method stub
		TextView title = (TextView) dialog.findViewById(R.id.contactsTitle);

		if (groups.size() == 0)
		{
			title.setText(context.getString(R.string.noGroups));
		} else {
			title.setText(context.getString(R.string.viewGroups));
			ArrayAdapter adapter1 = new ArrayAdapter(context, R.layout.dialog_contacts_row, R.id.contactEntryText, groups);
			lv.setAdapter(adapter1);
			lv.setFastScrollEnabled(false);
		}

	}






}
