package net.stargw.contactsimport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterForAccounts  extends ArrayAdapter<AccountData> {

	private AccountData[] data;
	private LayoutInflater inflater;
	
	public ArrayAdapterForAccounts(Context context, AccountData[] a) {
		super(context, R.layout.dialog_account_row, a);
		inflater = LayoutInflater.from(context);
		data = a;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View view = super.getView(position, convertView, parent);
		

		// textView.setTextAppearance(Global.getContext(), android.R.style.TextAppearance_Small);

        View row = convertView;
        DataHolder holder = null;
       
        if(row == null)
        {
            row = inflater.inflate(R.layout.dialog_account_row, parent, false);
           
            holder = new DataHolder();
            holder.text1 = (TextView)row.findViewById(R.id.text1);
            holder.text2 = (TextView)row.findViewById(R.id.text2);
           
            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }
       
        AccountData a = data[position];
        holder.text1.setText(a.account);
        holder.text2.setText(a.type);
       
        return row;
    }

	static class DataHolder
	{
		TextView text1;
		TextView text2;
	}

}
