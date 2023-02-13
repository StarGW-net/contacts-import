package net.stargw.contactsimport;

import android.app.AlertDialog;
import android.app.Dialog;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;


public class DialogDeleteGroups implements TaskCallback {

	
	Dialog dialog;
	Settings settings;
	Context context;
	TaskCallback callback;
	
	View myView;
	
	private ListView mainListView;
	private ArrayAdapter<ContactRecordDelete> listAdapter;

	ArrayList<ContactRecordDelete> groupList = new ArrayList<ContactRecordDelete>();

	private OnClickListener buttonDeleteListener = new OnClickListener() {
		public void onClick(View v) {
			CheckDeleteGroups();
		}
	};
	


	public void onTaskDone(int i) {
		// TODO Auto-generated method stub
		CheckBox checkBoxLog = (CheckBox) dialog.findViewById(R.id.deleteSelectAllCB);
		checkBoxLog.setChecked(false);
		DeSelectAll();
		UpdateDisplay("stuff");
		callback.onTaskDone(0);
	}

	
	private void UpdateDisplay(String text)
	{


		
		// ArrayAdapter listAdapter = new ArrayAdapter(this, R.layout.rowcheckbox, R.id.CheckBox01, items);
		Button buttonDelete = (Button) dialog.findViewById(R.id.deleteDeleteButton);
		TextView title = (TextView) dialog.findViewById(R.id.deleteSelectAllT);
		

		listAdapter = new SelectArralAdapter(context, groupList);
		if (groupList.size() == 0)
		{
			buttonDelete.setEnabled(false);
			// Global.infoMessage(this,"Warning!!","There are no contacts in account " + mappingsDelete.getAccountName());
			title.setText(context.getString(R.string.noGroups));
		} else {
			buttonDelete.setEnabled(true);
			title.setText(context.getString(R.string.deleteGroups));
		}
		
		mainListView.setAdapter(listAdapter);


	}
	

	

	public void onCreate(Context c, TaskCallback l) {

		context = c;
		callback = l;
		
		// dialog = new Dialog(context,R.style.dialog_full_screen);
		dialog = new Dialog(context,R.style.SWDialog);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		dialog.setContentView(R.layout.dialog_delete_main);
		
		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		
		// Find the ListView resource.
		mainListView = (ListView) dialog.findViewById(R.id.mainListView);


		mainListView.setFastScrollEnabled(true);
		
		mainListView
		.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
				ContactRecordDelete planet = listAdapter.getItem(position);
				planet.toggleChecked();
				SelectViewHolder viewHolder = (SelectViewHolder) item
						.getTag();
				viewHolder.getCheckBox().setChecked(planet.isChecked());

			}
		});
		
		CheckBox checkBoxAll = (CheckBox) dialog.findViewById(R.id.deleteSelectAllCB); // set checked here
		checkBoxAll.setChecked(false);
	
		// Set a listener for the checkbox
		OnClickListener checkBoxAllListener = new OnClickListener() {
			public void onClick(View v) {
				CheckBox checkBoxLog = (CheckBox) dialog.findViewById(R.id.deleteSelectAllCB);
				if (checkBoxLog.isChecked() == true)
				{
					Logs.myLog("Checkbox select all.",2);
					SelectAll();
					UpdateDisplay("Groups");
				} else {
					Logs.myLog("Checkbox select None.",2);
					DeSelectAll();
					UpdateDisplay("Groups");
				}
			};
		};
		
		checkBoxAll.setOnClickListener(checkBoxAllListener);
		
		Button buttonDelete = (Button) dialog.findViewById(R.id.deleteDeleteButton);
		buttonDelete.setOnClickListener(buttonDeleteListener);
		
		settings = new Settings();
		settings.load("del");
		
		dialog.show();
		
		// title.setText(getResources().getString(R.string.deleteGroups));
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskGetContactsDelete(true);
		myTaskController.myTaskGetContactsDelete = new TaskGetContactsDelete(myTaskController,context,groupList, this, settings, false);
		myTaskController.nextTask();

	}


	private void CheckDeleteGroups()
	{
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
		// set title
		alertDialogBuilder.setTitle(context.getString(R.string.deleteGroupsTitle));

		int n = 0;
		Iterator<ContactRecordDelete> it;

		it = groupList.iterator();
		while(it.hasNext())
		{
			ContactRecordDelete obj = it.next();
			//Do something with obj
			if (obj.isChecked())
			{
				n++;
			}
		}

		if (n > 0)
		{
			alertDialogBuilder.setMessage(String.format(context.getString(R.string.deleteGroupsPrompt),n));
		} else {
			alertDialogBuilder.setMessage(context.getString(R.string.noGroupsSelected));
		}
		
		final int num = n;
		
		// set dialog message
		alertDialogBuilder.setCancelable(false);
		
		if (n > 0)
		{
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						ProcessDeleteGroups(num);
					}
				});
		

			alertDialogBuilder.setNegativeButton(context.getString(android.R.string.cancel),new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
		} else {
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
		}
		
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
 
		// show it
		alertDialog.show();
	}


	
	
	private void ProcessDeleteGroups(int n)
	{
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskDeleteGroups(true);
		myTaskController.myTaskDeleteGroups = new TaskDeleteGroups(myTaskController,context,groupList, this, n);
		myTaskController.nextTask();
	}

	private void SelectAll()
	{
		
		Iterator<ContactRecordDelete> it;

		it = groupList.iterator();
		while(it.hasNext())
		{
			ContactRecordDelete obj = it.next();
			 //Do something with obj
			obj.setChecked(true);
		}


	}

	private void DeSelectAll()
	{
		
		Iterator<ContactRecordDelete> it;
		
		it = groupList.iterator();
		while(it.hasNext())
		{
			ContactRecordDelete obj = it.next();
			 //Do something with obj
			obj.setChecked(false);
		}


	}




		
}
