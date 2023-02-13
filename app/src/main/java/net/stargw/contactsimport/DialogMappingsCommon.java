package net.stargw.contactsimport;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class DialogMappingsCommon {

	public DialogMappingsCommon() {
		// TODO Auto-generated constructor stub
	}

	Mappings mappingsGeneric;
	String[] exampleRow;

	FragmentLifecycle callback;
	Context context;
	Dialog dialog;
	
	TextView fieldTextViews[] = new TextView[Global.fields];
	
	float scale; // complex calc. So do it only once!
	
	LinearLayout layout; // reference for whole screen
	

	
	//
	// Stub to be overridden
	//
	protected boolean processReset(int i)
	{
		// nothing
		return false;
	}

	
	//
	// Checkbox for dealing with header row
	//
	protected void addCheckBoxIgnoreFirstLine(final int id)
	{
		CheckBox checkBox = (CheckBox) dialog.findViewById(id); // set checked here
		 
		if (mappingsGeneric.getIgnoreFirstLine() == 1)
		{
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}

		// Set a listener for the checkbox
		OnClickListener checkBoxListener = new OnClickListener() {
			public void onClick(View v) {
				CheckBox checkBox = (CheckBox) dialog.findViewById(id); 
				if (checkBox.isChecked() == true)
				{
					mappingsGeneric.setIgnoreFirstLine(1);
				} else {
					mappingsGeneric.setIgnoreFirstLine(0);
				}
				mappingsGeneric.save();
			};
		};

		checkBox.setOnClickListener(checkBoxListener);
	}
	
	//
	// Checkbox for concat address
	// 
	protected void addCheckBoxConcatAddress(final int id)
	{

		CheckBox checkBoxConcatAddress = (CheckBox) dialog.findViewById(id); // set checked here
		 
		if (mappingsGeneric.getConcatAddress() == true)
		{
			checkBoxConcatAddress.setChecked(true);
		} else {
			checkBoxConcatAddress.setChecked(false);
		}

		// Set a listener for the checkbox
		OnClickListener checkBoxConcatAddressListener = new OnClickListener() {
			public void onClick(View v) {
				CheckBox checkBox = (CheckBox) dialog.findViewById(id); 
				if (checkBox.isChecked() == true)
				{
					mappingsGeneric.setConcatAddress(true);
				} else {
					mappingsGeneric.setConcatAddress(false);
				}
				mappingsGeneric.save();
			};
		};

		checkBoxConcatAddress.setOnClickListener(checkBoxConcatAddressListener);
		
	}

	class CreateFieldPickers extends AsyncTask<Void, Integer, Integer>
	{
		
		protected Integer doInBackground(Void...arg0) 
		{
			for (int j = 0; j < mappingsGeneric.getFieldNum(); j++)
			{
				publishProgress(j);
			}

			return 0;
		}
		
		protected void onPostExecute(Integer result)
		{
			// Add final divider
			LinearLayout divider = new LinearLayout(context);			
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 4);
			divider.setLayoutParams(layoutParams);
			layoutParams.topMargin = 10;
			layoutParams.bottomMargin = 10;
			divider.setBackgroundColor(Color.parseColor("#C0C0C0"));
			layout.addView(divider);
		}
		
		protected void onProgressUpdate(Integer...j)
		{

			int fieldNum = j[0];
				
			scale = context.getResources().getDisplayMetrics().density;
			
			// Add a divider first
			LinearLayout divider = new LinearLayout(context);			
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 4);
			divider.setLayoutParams(layoutParams);
			layoutParams.topMargin = 10;
			layoutParams.bottomMargin = 10;
			divider.setBackgroundColor(Color.parseColor("#C0C0C0"));
			layout.addView(divider);
			
			RelativeLayout layout2 = new RelativeLayout(context);
			
			TextView fieldLabel = new TextView(context);
			
	
			int pixels = (int) (100 * scale + 0.5f);
			// RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixels, RelativeLayout.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams2.addRule(getAlignField(), 1); 
			layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL,1);
			fieldLabel.setLayoutParams(layoutParams2);
			fieldLabel.setText(context.getString(R.string.field) + " " + (fieldNum + 1) + "  "); // Possible to use an XML style for this field or table cell?
			fieldLabel.setTextAppearance(context, android.R.style.TextAppearance_Small);
			layout2.addView(fieldLabel);
					
	
			
			TextView fieldText = new TextView(context);
			fieldTextViews[fieldNum] = fieldText;
	
			// fieldText.setHeight(10);
			pixels = (int) (150 * scale + 0.5f);
			RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(pixels, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams3.addRule(getAlignSpinner(), 1); 
			layoutParams3.addRule(RelativeLayout.CENTER_VERTICAL,1);
			// layoutParams3.setMargins(0,0,0,5);
	
	
			fieldText.setLayoutParams(layoutParams3);
	
			// fieldText.setBackgroundColor(Color.parseColor("#C0C0C0"));
			// fieldText.setBackgroundColor(Color.parseColor("#484848"));
			// fieldText.setBackgroundResource(R.drawable.dropdown_normal_holo_dark);
			// fieldText.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dropdown_normal_holo_dark));
			// fieldText.setTextSize(14);
			// fieldText.setHeight(10);
			
			// Logs.myLog("Display field: " + settings.getContactField(mappingsGeneric.getField(fieldNum)) , 1);
			
			fieldText.setBackgroundResource(R.drawable.button_steve);
			// fieldText.setTextColor(Color.parseColor("#ffffff"));
			fieldText.setTextAppearance(context, android.R.style.TextAppearance_Small);
			// fieldText.setText(" " + Global.getContactField(mappingsGeneric.getField(fieldNum)));
            fieldText.setText("" + Global.getContactField(mappingsGeneric.getField(fieldNum)));

            fieldText.setPadding(10, 10, 10, 10); // Maybe a prob?
			layout2.addView(fieldText);
			
	
			// do not add listener here or will fire when data populated
			// dropdown.setOnItemSelectedListener(dropdownListener);
			
			// Add the row
			layout.addView(layout2);
					
			// Add another row with an example field from the CSV if available. If export or no fields better check?
			if (exampleRow != null)
			{
				if (fieldNum < exampleRow.length)
				{
					LinearLayout layout3 = new LinearLayout(context);
					TextView exampleField = new TextView(context);
					exampleField.setText("(" + exampleRow[fieldNum] + ")");
					exampleField.setTextAppearance(context, android.R.style.TextAppearance_Small);
					layout3.addView(exampleField);
					layout.addView(layout3);
				}
			}
	
			final TextView cp = fieldText;
			final int f = fieldNum;
	
			
			fieldText.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					fieldPickerSelect(cp, f);
				}
			});
		}
	}
	
	public void fieldPickerSelect(final TextView cp, final int f)
	{
		AlertDialog.Builder builder = new Builder(context);

		builder.setTitle(context.getString(R.string.selectField));

		
		final int selected = mappingsGeneric.getField(f);
		
		builder.setSingleChoiceItems(Global.getContactFieldAll(), selected, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				// f is the field - item is the value
				cp.setText(" " + Global.getContactField(item));
				
				Logs.myLog("Changing " + f + " to " + item,3);
				if (Global.getContactFieldFlag(item) == false)
				{
					// Do we blank the old field?
					if (item != Global.NONE)
					{
						for (int i = 0; i < mappingsGeneric.getFieldNum(); i++)
						{
							if ( i != f)
							{
								if (mappingsGeneric.getField(i) == item)
								{
									mappingsGeneric.setField(i,Global.NONE);
									
									fieldTextViews[i].setText(Global.getContactField(mappingsGeneric.getField(i)));  // Set the text here
									
									Logs.myLog("Reset spinner  " + i + " to none.",2);
								}
							}
						}
					}
				}
				
				int previous = mappingsGeneric.getField(f);
				
				mappingsGeneric.setField(f, item);

				// User changed
				if (previous != item)
				{
					// Mappings now considered custom
					mappingsGeneric.setProfile(1);
					
					// Need to refresh the field
					resetPicker();
					
				}
				
				
				// Save
				mappingsGeneric.save();
				callback.onRefreshFragment();
				
				// Check to see if they have deselected given name or family names
				if  ( (previous == Global.GIVEN_NAME) ||
						(previous == Global.FAMILY_NAME) )
				{
					// only check this when real selection! And only once?
					if (mappingsGeneric.checkKeyFields() == false)
					{
						// Global.keyFieldWarn(Global.getContext());
						Global.keyFieldWarn(context);
					}
				}

				
				dialog.cancel();
			}});
		
		builder.show();
	}


	
	protected void resetPicker()
	{
		//Override this
	}
	
	protected void resetPickerDropdown(final TextView cp, final String[] options)
	{
		AlertDialog.Builder builder = new Builder(context);

		builder.setTitle(context.getString(R.string.selectFieldMappings));
		
		int selected = mappingsGeneric.getProfile();
		
		builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// mappingsGeneric.setProfile(item);
				cp.setText(options[item]);
				
				if (processReset(item) == true)
				{
					mappingsGeneric.setProfile(item);
					mappingsGeneric.save();
					callback.onRefreshFragment();
					dialog.cancel();
					return;
				}
				
				// Logs.myLog("Profile spinner pos " + pos + " selected.",2);
				
				mappingsGeneric.setProfile(item);

				for (int i = 0; i < mappingsGeneric.getFieldNum(); i++)
				{
					if (mappingsGeneric.getField(i) != -1)
					{
						fieldTextViews[i].setText(Global.getContactField(mappingsGeneric.getField(i)));
					}
				}
				mappingsGeneric.save();
				callback.onRefreshFragment();
				
				dialog.cancel();
			}});
		
		builder.show();
	}
	

	
	// Defaults for import - override for export
	protected int getAlignField()
	{

		return RelativeLayout.ALIGN_PARENT_LEFT; 
	}
	
	protected int getAlignSpinner()
	{
		return RelativeLayout.ALIGN_PARENT_RIGHT; 
	}
}
