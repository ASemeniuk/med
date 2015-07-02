package org.alexsem.medicine.transfer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "medicine", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Medicine (_id INTEGER PRIMARY KEY, name TEXT, description TEXT, typeId INTEGER, amount INTEGER, expireAt TEXT);");
        db.execSQL("CREATE TABLE MedType  (_id INTEGER PRIMARY KEY, type TEXT, unit TEXT, measurable INTEGER);");

        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('таблетки', 'шт.', 1)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('пакеты', 'шт.', 1)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('капсулы', 'шт.', 1)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('ампулы', 'шт.', 1)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('монодозы', 'шт.', 1)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('капли', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('свечи', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('спрей', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('раствор', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('мазь', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('гель', 'мл', 0)");
        db.execSQL("INSERT INTO MedType (type, unit, measurable) VALUES ('крем', 'мл', 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean updated = false;
        if (oldVersion <= 1) { //Migrate from version 1 to version 2
            updated = true;
        }
        if (!updated) { //Other cases
            db.execSQL("DROP TABLE IF EXISTS MedType");
            db.execSQL("DROP TABLE IF EXISTS Medicine");
            onCreate(db);
        }
    }

}
