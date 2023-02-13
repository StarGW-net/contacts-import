package net.stargw.contactsimport;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;



/** Custom adapter for displaying an array of Planet objects. */
public class SelectArralAdapter extends ArrayAdapter<ContactRecordDelete> {
	private LayoutInflater inflater;

	public SelectArralAdapter(Context context, List<ContactRecordDelete> planetList) {
		super(context, R.layout.dialog_delete_row, R.id.rowTextView, planetList);
		// Cache the LayoutInflate to avoid asking for a new one each time.
		inflater = LayoutInflater.from(context);
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Planet to display
		ContactRecordDelete planet = (ContactRecordDelete) this.getItem(position);

		// The child views in each row.
		CheckBox checkBox;
		TextView textView;

		// Create a new row view
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.dialog_delete_row, null);

			// Find the child views.
			textView = (TextView) convertView
					.findViewById(R.id.rowTextView);
			checkBox = (CheckBox) convertView.findViewById(R.id.CheckBox01);
			// Optimization: Tag the row with it's child views, so we don't
			// have to
			// call findViewById() later when we reuse the row.
			convertView.setTag(new SelectViewHolder(textView, checkBox));
			// If CheckBox is toggled, update the planet it is tagged with.
			
			// CheckBox checkBoxAll = (CheckBox)  getWindow().getDecorView().getRootView().findViewById(R.id.deleteSelectAllCB);
			final CheckBox checkBoxAll = (CheckBox)  ((ViewGroup) parent.getParent()).findViewById(R.id.deleteSelectAllCB);
			
			checkBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					ContactRecordDelete planet = (ContactRecordDelete) cb.getTag();
					planet.setChecked(cb.isChecked());
					if (!cb.isChecked())
					{
						// Reset the checked all
						// CheckBox checkBoxAll = (CheckBox)  getWindow().getDecorView().getRootView().findViewById(R.id.deleteSelectAllCB); // set checked here
						checkBoxAll.setChecked(false);
					}
				}
			});
		}
		// Reuse existing row view
		else {
			// Because we use a ViewHolder, we avoid having to call
			// findViewById().
			SelectViewHolder viewHolder = (SelectViewHolder) convertView
					.getTag();
			checkBox = viewHolder.getCheckBox();
			textView = viewHolder.getTextView();
		}

		// Tag the CheckBox with the Planet it is displaying, so that we can
		// access the planet in onClick() when the CheckBox is toggled.
		checkBox.setTag(planet);
		// Display planet data
		checkBox.setChecked(planet.isChecked());
		textView.setText(planet.getmText());
		return convertView;
	}

	private Context getActivity() {
		// TODO Auto-generated method stub
		return null;
	}
}


