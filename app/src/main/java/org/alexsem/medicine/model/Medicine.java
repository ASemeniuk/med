package org.alexsem.medicine.model;

import android.os.Parcel;
import android.os.Parcelable;
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
public class Medicine implements Comparable<Medicine>, Parcelable {

    private long id;
    private long groupId;
    private String name;
    private String description;
    private String link;
    private long typeId;
    private int amount;
    private Date expireAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public Medicine() {
    }

    private Medicine(Parcel in) {
        this.groupId = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.link = in.readString();
        this.typeId = in.readLong();
        this.amount = in.readInt();
        this.expireAt = new Date(in.readLong());
    }

    @Override
    public int compareTo(@NonNull Medicine medicine) {
        return this.name.compareTo(medicine.name);
    }

    public static final Parcelable.Creator<Medicine> CREATOR = new Parcelable.Creator<Medicine>() {

        public Medicine createFromParcel(Parcel in) {
            return new Medicine(in);
        }

        public Medicine[] newArray(int size) {
            return new Medicine[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(groupId);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(link);
        parcel.writeLong(typeId);
        parcel.writeInt(amount);
        parcel.writeLong(expireAt.getTime());
    }

    /**
     * Converts Medicine object top JSONObject
     * @return Generated JSON
     * @throws JSONException in case JSON formatting fails
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("groupId", groupId);
        json.put("name", name);
        json.put("description", description);
        json.put("link", link);
        json.put("typeId", typeId);
        json.put("amount", amount);
        json.put("expireAt", MedicineProvider.formatExpireDate(expireAt));
        return json;
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
        medicine.setGroupId(json.getLong("groupId"));
        medicine.setName(json.getString("name"));
        medicine.setDescription(json.optString("description", ""));
        medicine.setDescription(json.optString("link", ""));
        medicine.setTypeId(json.getLong("typeId"));
        medicine.setAmount(json.optInt("amount", 0));
        medicine.setExpireAt(MedicineProvider.parseExpireDate(json.getString("expireAt")));
        return medicine;
    }
}
