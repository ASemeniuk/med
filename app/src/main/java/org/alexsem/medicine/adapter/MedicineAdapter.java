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

import java.text.ParseException;
import java.util.Date;


/**
 * Adapter used to represent list of medicine items
 * @author Semeniuk A.D.
 */
public class MedicineAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public MedicineAdapter(Context context, Cursor data) {
        super(context, data, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View row = mInflater.inflate(R.layout.row_medicine, parent, false);
        ViewHolder holder = new ViewHolder(row);
        row.setTag(holder);
        return row;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.getName().setText(cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.NAME)));
        holder.getType().setText(String.format("(%s)", cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.TYPE))));
        holder.getAmount().setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(MedicineProvider.Medicine.AMOUNT))));
        holder.getUnit().setText(cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.UNIT)));
        try {
            String date = cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.EXPIRATION));
            String now = MedicineProvider.formatExpireDate(new Date());
            view.getBackground().setLevel(now.compareTo(date) > 0 ? 2 : now.equals(date) ? 1 : 0);
            holder.getExpiration().setText(String.format("%1$tb. %1$tY", MedicineProvider.parseExpireDate(date)));
        } catch (ParseException e) {
            holder.getExpiration().setText("");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Class used for temporary data storage
     * @author Semeniuk A.D.
     */
    private static class ViewHolder {
        private View base;
        private TextView name = null;
        private TextView type = null;
        private TextView amount = null;
        private TextView unit = null;
        private TextView expiration = null;

        /**
         * Constructor
         * @param base Parent view
         */
        public ViewHolder(View base) {
            this.base = base;
        }

        public TextView getName() {
            if (name == null) {
                name = (TextView) base.findViewById(R.id.medicine_name);
            }
            return (name);
        }

        public TextView getType() {
            if (type == null) {
                type = (TextView) base.findViewById(R.id.medicine_type);
            }
            return (type);
        }

        public TextView getAmount() {
            if (amount == null) {
                amount = (TextView) base.findViewById(R.id.medicine_amount);
            }
            return (amount);
        }

        public TextView getUnit() {
            if (unit == null) {
                unit = (TextView) base.findViewById(R.id.medicine_unit);
            }
            return (unit);
        }

        public TextView getExpiration() {
            if (expiration == null) {
                expiration = (TextView) base.findViewById(R.id.medicine_expiration);
            }
            return (expiration);
        }

    }

}
