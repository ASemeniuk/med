package org.alexsem.medicine.model;

import android.support.annotation.NonNull;

import org.alexsem.medicine.transfer.MedicineProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Class which contains data for one specific medicine item
 * @author Semeniuk A.D.
 */
public class Medicine implements Comparable<Medicine> {

    private long id;
    private String name;
    private String description;
    private long typeId;
    private int amount;
    private Date expireAt;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    @Override
    public int compareTo(@NonNull Medicine medicine) {
        return this.name.compareTo(medicine.name);
    }

    /**
     * Parses Medicine object from JSONObject
     * @param json JSONObject to parse
     * @return parsed object
     * @throws org.json.JSONException   in case parsing failed
     * @throws java.text.ParseException in case date parsing failed
     */
    public static Medicine fromJSON(JSONObject json) throws JSONException, ParseException {
        Medicine medicine = new Medicine();
        medicine.setId(json.getLong("id"));
        medicine.setName(json.getString("name"));
        medicine.setDescription(json.getString("description"));
        medicine.setTypeId(json.getLong("typeId"));
        medicine.setAmount(json.getInt("amount"));
        medicine.setExpireAt(MedicineProvider.parseExpireDate(json.getString("expireAt")));
        return medicine;
    }
}
