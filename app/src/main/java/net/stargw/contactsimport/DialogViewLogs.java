package net.stargw.contactsimport;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DialogViewLogs {

	public DialogViewLogs() {
		// TODO Auto-generated constructor stub
	}

	Dialog dialog;
	Context context;
	
	public void onCreate(Context c) {
		
		context = c;
		dialog = new Dialog(context,R.style.SWDialog);
		// dialog = new Dialog(context,R.style.dialog_full_screen);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.dialog_logs);
		
		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		
		logLevelPicker();
		
		/*
		LinearLayout lv = (LinearLayout) dialog.findViewById(R.id.logText);
				
		dialog.show();
		
		TaskController myTaskController = new TaskController();
		myTaskController.setTaskGetLogs(true);
		myTaskController.myTaskGetLogs = new TaskGetLogs(myTaskController,context, lv);
		myTaskController.nextTask();
		// want a clear log button
		*/
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.dialog_logs_row, Logs.getLogBufferList());
		
		ListView lv = (ListView) dialog.findViewById(R.id.logs);
		lv.setAdapter(adapter);
		
		Button logsClear = (Button) dialog.findViewById(R.id.logsClearButton);
		logsClear.setOnClickListener(buttonLogsClear); 
		
		dialog.show();
		
	}
	
	private OnClickListener buttonLogsClear = new OnClickListener() {
		public void onClick(View v) {
			Logs.clearLog();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.dialog_logs_row, Logs.getLogBufferList());
			
			ListView lv = (ListView) dialog.findViewById(R.id.logs);
			lv.setAdapter(adapter);
		}
	};
	
	protected void logLevelPicker()
	{
		final String options[] = context.getResources().getStringArray(R.array.logLevels);
		
		final TextView cp = (TextView) dialog.findViewById(R.id.logsLoggingLevelPicker);
		
		cp.setText(options[Logs.getLoggingLevel() - 1]);
		
		cp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				logLevelPickerDropdown(cp, options);
			}
		});
	}
	
	
	
	protected void logLevelPickerDropdown(final TextView cp, final String[] options)
	{
		AlertDialog.Builder builder = new Builder(context);

		builder.setTitle(context.getString(R.string.text21));
		
		int selected = Logs.getLoggingLevel() - 1;
		
		builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.cancel();
				if (( item + 1) != Logs.getLoggingLevel())
				{
					cp.setText(options[item]);
					Logs.setLoggingLevel(item + 1);
					warn(item + 1, options[item]);
				}
			}});
		
		builder.show();
	}
	
	private void warn(int level, String levelText)
	{
		String message = context.getString(R.string.text22);
		if (level > 1)
		{
			message = message + "\n\n" + String.format(context.getString(R.string.text23), levelText);
		}
		
		Global.infoMessage(context,context.getString(R.string.warning),message);
	}
}
