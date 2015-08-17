package org.alexsem.medicine.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.alexsem.medicine.R;

/**
 * Adapter for showing list of months
 * @author Semeniuk A.D.
 */
public class MedicineYearAdapter extends BaseAdapter implements SpinnerAdapter {

    public final int MIN_YEAR = 2000;
    public final int MAX_YEAR = 2099;


    private LayoutInflater mInflater = null;

    public MedicineYearAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return MAX_YEAR - MIN_YEAR + 1;
    }

    @Override
    public Integer getItem(int position) {
        return MIN_YEAR + position;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        row.setText(String.valueOf(getItem(position)));
        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) mInflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
        }
        row.setText(String.valueOf(getItem(position)));
        return row;
    }

}