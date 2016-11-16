package org.alexsem.medicine.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.alexsem.medicine.R;

public class DrawerAdapter extends ArrayAdapter<int[]> {

    public DrawerAdapter(Context context, int[][] objects) {
        super(context, R.layout.row_drawer, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_drawer, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int[] data = getItem(position);
        holder.getIcon().setImageResource(data[0]);
        holder.getText().setText(getContext().getString(data[1]));

        return convertView;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Class used for temporary data storage
     * @author Semeniuk A.D.
     */
    private static class ViewHolder {
        private View base;
        private ImageView icon = null;
        private TextView text = null;

        /**
         * Constructor
         * @param base Parent view
         */
        public ViewHolder(View base) {
            this.base = base;
        }

        public ImageView getIcon() {
            if (icon == null) {
                icon = (ImageView) base.findViewById(android.R.id.icon);
            }
            return (icon);
        }

        public TextView getText() {
            if (text == null) {
                text = (TextView) base.findViewById(android.R.id.text1);
            }
            return (text);
        }

    }
}
