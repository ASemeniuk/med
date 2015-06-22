package org.alexsem.medicine.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class which contains data for one specific medicine type
 * @author Semeniuk A.D.
 */
public class MedicineType {

    private long id;
    private String type;
    private String unit;
    private boolean measurable;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isMeasurable() {
        return measurable;
    }

    public void setMeasurable(boolean measurable) {
        this.measurable = measurable;
    }

    /**
     * Parses MedicineType object from JSONObject
     * @param json JSONObject to parse
     * @return parsed object
     * @throws JSONException in case parsing failed
     */
    public static MedicineType fromJSON(JSONObject json) throws JSONException {
        MedicineType type = new MedicineType();
        type.setId(json.getLong("id"));
        type.setType(json.getString("type"));
        type.setUnit(json.getString("unit"));
        type.setMeasurable(json.getBoolean("measurable"));
        return type;
    }
}
