package org.alexsem.medicine.model;

import android.support.annotation.NonNull;

import org.alexsem.medicine.transfer.MedicineProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Class which contains data for one specific medicine group
 * @author Semeniuk A.D.
 */
public class MedicineGroup implements Comparable<MedicineGroup> {

    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NonNull MedicineGroup group) {
        return this.name.compareTo(group.name);
    }

    /**
     * Converts MedicineGroup object top JSONObject
     * @return Generated JSON
     * @throws JSONException in case JSON formatting fails
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        return json;
    }

    /**
     * Parses MedicineGroup object from JSONObject
     * @param json JSONObject to parse
     * @return parsed object
     * @throws JSONException   in case parsing failed
     * @throws ParseException in case date parsing failed
     */
    public static MedicineGroup fromJSON(JSONObject json) throws JSONException, ParseException {
        MedicineGroup group = new MedicineGroup();
        group.setId(json.getLong("id"));
        group.setName(json.getString("name"));
        return group;
    }
}
