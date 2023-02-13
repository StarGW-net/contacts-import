package net.stargw.contactsimport;

import android.app.AlertDialog;
import android.app.Dialog;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class DialogDeleteContacts implements TaskCallback {

	
	Settings settings;
	Context context;
	Dialog dialog;
	TaskCallback callback;
	
	View myView;
	
	private ListView mainListView;
	private ArrayAdapter<ContactRecordDelete> listAdapter;

	ArrayList<ContactRecordDelete> rawContactsList = new ArrayList<ContactRecordDelete>();



	private OnClickListener buttonDeleteListener = new OnClickListener() {
		public void onClick(View v) {
			CheckDeleteContacts();
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
		
		listAdapter = new SelectArralAdapter(context, rawContactsList);
		if (rawContactsList.size() == 0)
		{
			buttonDelete.setEnabled(false);
			// Global.infoMessage(this,"Warning!!","There are no contacts in account " + mappingsDelete.getAccountName());
			title.setText(context.getString(R.string.noContacts));
		} else {
			buttonDelete.setEnabled(true);
			title.setText(context.getResources().getString(R.string.deleteContacts));
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
					Logs.myLog("Checkbox select all.",1);
					SelectAll();
					UpdateDisplay("Raw Contacts");
				} else {
					Logs.myLog("Checkbox select None.",1);
					DeSelectAll();
					UpdateDisplay("Raw Contacts");
				}
			};
		};
		
		checkBoxAll.setOnClickListener(checkBoxAllListener);
		
		Button buttonDelete = (Button) dialog.findViewById(R.id.deleteDeleteButton);
		buttonDelete.setOnClickListener(buttonDeleteListener);
		
		settings = new Settings();
		settings.load("del");
		
		dialog.show();
		
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskGetContactsDelete(true);
		myTaskController.myTaskGetContactsDelete = new TaskGetContactsDelete(myTaskController,context,rawContactsList, this, settings, true);
		myTaskController.nextTask();
		
	}


	private void CheckDeleteContacts()
	{

		int n = 0;
		Iterator<ContactRecordDelete> it;

		it = rawContactsList.iterator();
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
			// alertDialogBuilder.setMessage("Are you sure you want to delete " + n + " Contacts?");
			actionPrompt(n);
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			 
			// set title
			alertDialogBuilder.setTitle(context.getString(R.string.deleteContactsTitle));
			alertDialogBuilder.setMessage(context.getString(R.string.noContactsSelected));
			alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
		
			// show it
			alertDialog.show();
		}
	}
	

	private void SelectAll()
	{
		
		Iterator<ContactRecordDelete> it;

		it = rawContactsList.iterator();
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

		it = rawContactsList.iterator();
		while(it.hasNext())
		{
			ContactRecordDelete obj = it.next();
			 //Do something with obj
			obj.setChecked(false);
		}


	}

	/*
	public Object onRetainNonConfigurationInstance() {
		Object itemss = null;
		return itemss;
	}
	*/
	
	private void actionPrompt(int n)
	{
		boolean backup = true;
		
		Dialog dialog = new Dialog(context);
		
		dialog.setContentView(R.layout.dialog_action_prompt);
		dialog.setTitle(context.getString(R.string.warning));
		
		TextView text1 = (TextView) dialog.findViewById(R.id.dialogActionText);
		/*
		text1.setText("Delete " + num + " contacts from account \"" + Global.accountName + "\"" 
				+ " and import " + (Global.peopleImport.size() - mappingsImport.getIgnoreFirstLine())
				+ " contacts from file:");
		*/
		
		text1.setText(String.format(context.getString(R.string.dialogAction04), n, settings.getAccountName()));
		
		TextView text2 = (TextView) dialog.findViewById(R.id.dialogActionFile);
		text2.setVisibility(View.GONE);
		

		// Do not offer a back up if its not our account
		if (settings.getAccountName().equals(Global.accountName))
		{
			TextView text3 = (TextView) dialog.findViewById(R.id.dialogActionCBText);
			text3.setText(context.getString(R.string.dialogAction01));
		} else {
			LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.dialogActionOption);
			layout.setVisibility(View.GONE);
			backup = false; // so we do not check the checkbox
		}
		
		Button dialogButtonOK = (Button) dialog.findViewById(R.id.dialogActionOK);

		final Dialog fdialog = dialog;
		final int num = n;
		final boolean fbackup = backup;
		
		dialogButtonOK.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				fdialog.dismiss();
				if (fbackup != false)
				{
					CheckBox checkBox = (CheckBox) fdialog.findViewById(R.id.dialogActionCB);
					actionExecute(checkBox.isChecked(),num);
				} else {
					actionExecute(false,num);
				}

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

	}

	void actionExecute(boolean delete, int n)
	{
		TaskController myTaskController = new TaskController();
		if (delete == true)
		{
			myTaskController.setTaskBackup(true);
			myTaskController.myTaskBackup = new TaskBackup(myTaskController,context,1,false, null); // UTF codepage
		}
		
		myTaskController.setTaskDeleteContacts(true);
		myTaskController.myTaskDeleteContacts = new TaskDeleteContacts(myTaskController,context,rawContactsList, this, n);

		myTaskController.nextTask();
	}





	

		
}
