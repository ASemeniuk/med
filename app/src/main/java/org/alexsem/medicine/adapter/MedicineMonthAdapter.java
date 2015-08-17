package org.alexsem.medicine.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.alexsem.medicine.R;

/**
 * Adapter for showing list of months
 * @author Semeniuk A.D.
 */
public class MedicineMonthAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater = null;

    public MedicineMonthAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.edit_months));
        this.mInflater = LayoutInflater.from(context);
        setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        row.setText(getItem(position));
        return row;
    }

}