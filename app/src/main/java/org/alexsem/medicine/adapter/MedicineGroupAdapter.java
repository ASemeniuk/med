package org.alexsem.medicine.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.alexsem.medicine.R;
import org.alexsem.medicine.transfer.MedicineProvider;

/**
 * Adapter for showing list of medicine groups
 * @author Semeniuk A.D.
 */
public class MedicineGroupAdapter extends CursorAdapter {

    private LayoutInflater mInflater = null;

    public MedicineGroupAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String type = cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineGroup.NAME));
        ((TextView) view).setText(type);
    }

}