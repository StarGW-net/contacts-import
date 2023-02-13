package net.stargw.contactsimport;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterWithIcon  extends ArrayAdapter<String> {

	private List<Integer> images;

	
	public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = Arrays.asList(images);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setTextAppearance(Global.getContext(), android.R.style.TextAppearance_Small);
		textView.setTextColor(Color.WHITE);
		textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
		textView.setCompoundDrawablePadding(
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
		// TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
		return view;
	}

}
