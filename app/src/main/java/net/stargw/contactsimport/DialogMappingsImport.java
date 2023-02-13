package net.stargw.contactsimport;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class DialogMappingsImport extends DialogMappingsCommon {



	
	private OnClickListener buttonDoneListener = new OnClickListener() {
		public void onClick(View v) {
			dialog.dismiss();
		}
	};
	

	public void onCreate(Context c, FragmentLifecycle l) {

		context = c;
		callback = l;
		
		// dialog = new Dialog(context,R.style.dialog_full_screen);
		dialog = new Dialog(context,R.style.SWDialog);

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.dialog_mappings_import);
		
		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		
		mappingsGeneric = new Mappings();
		mappingsGeneric.load();

		// We should have loaded the CSV before now
		if (Global.peopleImport != null)
		{
			if (Global.peopleImport.size() < 1)
			{
				exampleRow = new String[1];
				exampleRow[0] = context.getString(R.string.noExampleCSV); 
			} else {
				// exampleRow = Global.peopleImport.get(0).getmText();
				exampleRow = Global.peopleImport.get(0);
				// we could use another row as an example...   
			}
		}
		
		// Add the two checkboxes
		addCheckBoxIgnoreFirstLine(R.id.mapImportIgnoreFirstLineCB);
		addCheckBoxConcatAddress(R.id.mapImportConcatAddressCB);
		
		resetPicker();
		
		// Get layout of field spinners part of screen
		layout = (LinearLayout) dialog.findViewById(R.id.mapImportFields);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		dialog.show();
		
		CreateFieldPickers createFieldPickers = new CreateFieldPickers();
		createFieldPickers.execute();
		
		Button button = (Button) dialog.findViewById(R.id.mappingsDoneButton);
		button.setOnClickListener(buttonDoneListener);

	}
	
	@Override
	protected boolean processReset(int pos)
	{
		if (pos == 0) 
		{
			mappingsGeneric.defaultPrefs();
		}
		if (pos == 1) // None
		{
			// custom
			return true;
		}
		if (pos == 2)
		{
			mappingsGeneric.defaultOutlookEnglishPrefs();
		}
		if (pos == 3)
		{
			mappingsGeneric.defaultOutlookSpanishPrefs();
		}
		if (pos == 4)
		{
			mappingsGeneric.defaultThunderbirdPrefs();
		}
		if (pos == 5)
		{
			mappingsGeneric.defaultStevePrefs();
		}
		if (pos == 6)
		{
			mappingsGeneric.defaultFastmailPrefs();
		}
		return false;
	}
	
	@Override
	protected void resetPicker()
	{
		final String options[] = context.getResources().getStringArray(R.array.importProfilesFriendly);
		
		int id = R.id.mapImportResetPicker;
				
		
		final TextView cp = (TextView) dialog.findViewById(id);
		
		cp.setText(options[mappingsGeneric.getProfile()]);
		
		cp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				resetPickerDropdown(cp, options);
			}
		});
	}

}