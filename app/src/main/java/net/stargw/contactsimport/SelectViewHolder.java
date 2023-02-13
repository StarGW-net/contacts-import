package net.stargw.contactsimport;

import android.widget.CheckBox;
import android.widget.TextView;

/** Holds child views for one row. */
public class SelectViewHolder {
	private CheckBox checkBox;
	private TextView textView;

	public SelectViewHolder() {
	}

	public SelectViewHolder(TextView textView, CheckBox checkBox) {
		this.checkBox = checkBox;
		this.textView = textView;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}


	public TextView getTextView() {
		return textView;
	}

}
