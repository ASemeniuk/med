package org.alexsem.medicine.transfer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.alexsem.medicine.model.Medicine;
import org.alexsem.medicine.model.MedicineGroup;
import org.alexsem.medicine.model.MedicineType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Contains methods to retrieve and deploy DB data in JSON format
 */
public abstract class ImportExportManager {

    /**
     * Represents database contents as JSON string
     * @param context Context
     * @return Generated string representation of the database
     * @throws JSONException in case JSON parsing fails
     * @throws ParseException in case date parsing fails
     */
    public static String export(Context context) throws JSONException, ParseException {
        JSONObject jData = new JSONObject();

        //---- Load Medicine Type data ----
        Uri uri1 = MedicineProvider.MedicineType.CONTENT_URI;
        String[] projection1 = {
                MedicineProvider.MedicineType.ID,
                MedicineProvider.MedicineType.TYPE,
                MedicineProvider.MedicineType.UNIT,
                MedicineProvider.MedicineType.MEASURABLE
        };
        Cursor cursor1 = context.getContentResolver().query(uri1, projection1, null, null, null);
        JSONArray jTypes = new JSONArray();
        try {
            if (cursor1 != null && cursor1.moveToFirst()) {
                do {
                    MedicineType type = new MedicineType();
                    type.setId(cursor1.getLong(cursor1.getColumnIndex(MedicineProvider.MedicineType.ID)));
                    type.setType(cursor1.getString(cursor1.getColumnIndex(MedicineProvider.MedicineType.TYPE)));
                    type.setUnit(cursor1.getString(cursor1.getColumnIndex(MedicineProvider.MedicineType.UNIT)));
                    type.setMeasurable(cursor1.getInt(cursor1.getColumnIndex(MedicineProvider.MedicineType.MEASURABLE)) == 1);
                    jTypes.put(type.toJSON());
                } while (cursor1.moveToNext());
            }
        } finally {
            if (cursor1 != null) {
                cursor1.close();
            }
        }
        jData.put("types", jTypes);

        //---- Load Medicine Group data ----
        Uri uri2 = MedicineProvider.MedicineGroup.CONTENT_URI;
        String[] projection2 = {
                MedicineProvider.MedicineGroup.ID,
                MedicineProvider.Medicine.NAME
        };
        Cursor cursor2 = context.getContentResolver().query(uri2, projection2, null, null, null);
        JSONArray jGroups = new JSONArray();
        try {
            if (cursor2 != null && cursor2.moveToFirst()) {
                do {
                    MedicineGroup group = new MedicineGroup();
                    group.setId(cursor2.getLong(cursor2.getColumnIndex(MedicineProvider.MedicineGroup.ID)));
                    group.setName(cursor2.getString(cursor2.getColumnIndex(MedicineProvider.MedicineGroup.NAME)));
                    jGroups.put(group.toJSON());
                } while (cursor2.moveToNext());
            }
        } finally {
            if (cursor2 != null) {
                cursor2.close();
            }
        }
        jData.put("groups", jGroups);

        //---- Load Medicine data ----
        Uri uri3 = MedicineProvider.Medicine.CONTENT_URI;
        String[] projection3 = {
                MedicineProvider.Medicine.ID,
                MedicineProvider.Medicine.GROUP_ID,
                MedicineProvider.Medicine.NAME,
                MedicineProvider.Medicine.DESCRIPTION,
                MedicineProvider.Medicine.LINK,
                MedicineProvider.Medicine.TYPE_ID,
                MedicineProvider.Medicine.AMOUNT,
                MedicineProvider.Medicine.EXPIRATION,
        };
        Cursor cursor3 = context.getContentResolver().query(uri3, projection3, null, null, null);
        JSONArray jMedicine = new JSONArray();
        try {
            if (cursor3 != null && cursor3.moveToFirst()) {
                do {
                    Medicine medicine = new Medicine();
                    medicine.setId(cursor3.getLong(cursor3.getColumnIndex(MedicineProvider.Medicine.ID)));
                    medicine.setGroupId(cursor3.getLong(cursor3.getColumnIndex(MedicineProvider.Medicine.GROUP_ID)));
                    medicine.setName(cursor3.getString(cursor3.getColumnIndex(MedicineProvider.Medicine.NAME)));
                    medicine.setDescription(cursor3.getString(cursor3.getColumnIndex(MedicineProvider.Medicine.DESCRIPTION)));
                    medicine.setLink(cursor3.getString(cursor3.getColumnIndex(MedicineProvider.Medicine.LINK)));
                    medicine.setTypeId(cursor3.getLong(cursor3.getColumnIndex(MedicineProvider.Medicine.TYPE_ID)));
                    medicine.setAmount(cursor3.getInt(cursor3.getColumnIndex(MedicineProvider.Medicine.AMOUNT)));
                    medicine.setExpireAt(MedicineProvider.parseExpireDate(cursor3.getString(cursor3.getColumnIndex(MedicineProvider.Medicine.EXPIRATION))));
                    jMedicine.put(medicine.toJSON());
                } while (cursor3.moveToNext());
            }
        } finally {
            if (cursor3 != null) {
                cursor3.close();
            }
        }
        jData.put("medicine", jMedicine);

        return jData.toString(2);
    }

}
