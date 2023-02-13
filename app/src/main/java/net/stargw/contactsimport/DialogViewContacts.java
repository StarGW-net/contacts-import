package net.stargw.contactsimport;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DialogViewContacts implements TaskCallback {

	Settings settings;
	String action;
	
	Dialog dialog;
	Context context;


	// Global variables :-)
	private ListView lv;
	private List<ContactRecordView> people = new ArrayList<ContactRecordView>();
	int state = 0; // List ALL Groups
	


	/** Called when the activity is first created. */

	public void onCreate(Context c, int state, long group) {
		
		context = c;
		// dialog = new Dialog(context,R.style.dialog_full_screen);
		dialog = new Dialog(context,R.style.SWDialog);

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
		
		// state = 2;
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskGetContactsView(true);
		myTaskController.myTaskGetContactsView= new TaskGetContactsView(myTaskController,context,people, this, settings, state, group);
		myTaskController.nextTask();



		
	}


	//
	// Handle when an item is selected from the ListView
	//
	private void Selection(int position)
	{
		long n = 0;
		
		n = people.get(position).getId();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Long.toString(n));
		intent.setData(uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // stop the contact app grabbing our task.
		context.startActivity(intent);

	}


	public void onTaskDone(int i) {
		// TODO Auto-generated method stub
		TextView title = (TextView) dialog.findViewById(R.id.contactsTitle);
		

		if (people.size() == 0)
		{
			title.setText(context.getString(R.string.noContacts));
		} else {
			title.setText(context.getString(R.string.viewContacts));
			ArrayAdapter adapter1 = new ArrayAdapter(context, R.layout.dialog_contacts_row, R.id.contactEntryText, people);
			lv.setAdapter(adapter1);
			lv.setFastScrollEnabled(true);
		}

	}






}
