package org.alexsem.medicine.transfer;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Provider which is used to access internal database
 * @author Semeniuk A.D.
 */
public class MedicineProvider extends ContentProvider {

    public static final String AUTHORITY = "org.alexsem.medicine";

    //--------------------------------------------------------------------------------------------------------------------

    private static final SimpleDateFormat EXPIRE_FORMAT = new SimpleDateFormat("yyyy-MM");

    /**
     * Converts string representation of the date to Date object
     * @param date Date string to parse
     * @return Parsed Date object
     * @throws ParseException in case parsing fails
     */
    public static Date parseExpireDate(String date) throws ParseException {
        Date result;
        synchronized (EXPIRE_FORMAT) {
            result = EXPIRE_FORMAT.parse(date);
        }
        return result;
    }

    /**
     * Converts Date object to string representation
     * @param date Date to format
     * @return Formatted string
     */
    public static String formatExpireDate(Date date) {
        String result;
        synchronized (EXPIRE_FORMAT) {
            result = EXPIRE_FORMAT.format(date);
        }
        return result;
    }

    //--------------------------------------------------------------------------------------------------------------------

    public static final class Medicine {
        public static final String _T = "Medicine";
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String TYPE_ID = "typeId";
        public static final String AMOUNT = "amount";
        public static final String EXPIRATION = "expireAt";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/medicine");
    }

    public static final class MedicineType {
        public static final String _T = "MedType";
        public static final String ID = "_id";
        public static final String TYPE = "type";
        public static final String UNIT = "unit";
        public static final String MEASURABLE = "measurable";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/medtype");
    }

    //--------------------------------------------------------------------------------------------------------------------

    private DatabaseHelper mHelper;

    private static final int MEDICINE_ALL = 11;
    private static final int MEDICINE_TYPE = 12;
    private static final int MEDICINE_SINGLE = 13;
    private static final int MEDTYPE_ALL = 21;
    private static final int MEDTYPE_SINGLE = 22;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, "medicine", MEDICINE_ALL);
        uriMatcher.addURI(AUTHORITY, "medicine/type/#", MEDICINE_TYPE);
        uriMatcher.addURI(AUTHORITY, "medicine/#", MEDICINE_SINGLE);

        uriMatcher.addURI(AUTHORITY, "medtype", MEDICINE_ALL);
        uriMatcher.addURI(AUTHORITY, "medtype/#", MEDTYPE_SINGLE);
    }

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return false;
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MEDICINE_ALL:
            case MEDICINE_TYPE:
                return String.format("vnd.android.cursor.dir/vnd.%s.medicine", AUTHORITY);
            case MEDICINE_SINGLE:
                return String.format("vnd.android.cursor.item/vnd.%s.medicine", AUTHORITY);
            case MEDTYPE_ALL:
                return String.format("vnd.android.cursor.dir/vnd.%s.medtype", AUTHORITY);
            case MEDTYPE_SINGLE:
                return String.format("vnd.android.cursor.item/vnd.%s.medtype", AUTHORITY);
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case MEDICINE_ALL:
                queryBuilder.setTables(String.format("%1$s inner join %2$s on %1$s.%3$s = %2$s.%4$s", Medicine._T, MedicineType._T, Medicine.TYPE_ID, MedicineType.ID));
                selection = null;
                selectionArgs = null;
                sortOrder = String.format("%s collate nocase", Medicine.NAME);
                for (int i = 0; i < projection.length; i++) {
                    if (projection[i].equals(Medicine.ID)) {
                        projection[i] = String.format("%s.%s", Medicine._T, Medicine.ID);
                    }
                }
                break;
            case MEDICINE_TYPE:
                queryBuilder.setTables(String.format("%1$s inner join %2$s on %1$s.%3$s = %2$s.%4$s", Medicine._T, MedicineType._T, Medicine.TYPE_ID, MedicineType.ID));
                selection = (String.format("%s = ?", Medicine.TYPE_ID));
                selectionArgs = new String[]{uri.getLastPathSegment()};
                sortOrder = String.format("%s collate nocase", Medicine.NAME);
                for (int i = 0; i < projection.length; i++) {
                    if (projection[i].equals(Medicine.ID)) {
                        projection[i] = String.format("%s.%s", Medicine._T, Medicine.ID);
                    }
                }
                break;
            case MEDICINE_SINGLE:
                queryBuilder.setTables(Medicine._T);
                selection = (String.format("%s = ?", Medicine.ID));
                selectionArgs = new String[]{uri.getLastPathSegment()};
                sortOrder = null;
                break;

            case MEDTYPE_ALL:
                queryBuilder.setTables(MedicineType._T);
                selection = null;
                selectionArgs = null;
                sortOrder = null;
                break;
            case MEDTYPE_SINGLE:
                queryBuilder.setTables(MedicineType._T);
                selection = String.format("%s = ?", MedicineType.ID);
                selectionArgs = new String[]{uri.getLastPathSegment()};
                sortOrder = null;
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String table;

        switch (uriMatcher.match(uri)) {
            case MEDICINE_ALL:
                table = Medicine._T;
                break;
            case MEDTYPE_ALL:
                table = MedicineType._T;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        long id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(String.format("%s/%d", uri.toString(), id));
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String table;

        switch (uriMatcher.match(uri)) {
            case MEDICINE_SINGLE:
                table = Medicine._T;
                selection = (String.format("%s = ?", Medicine.ID));
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case MEDTYPE_SINGLE:
                table = MedicineType._T;
                selection = (String.format("%s = ?", MedicineType.ID));
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        int count = db.update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String table;
        List<String> segments;

        switch (uriMatcher.match(uri)) {
            case MEDICINE_ALL:
                table = Medicine._T;
                selection = null;
                selectionArgs = null;
                break;
            case MEDICINE_SINGLE:
                table = Medicine._T;
                selection = String.format("%s = ?", Medicine.ID);
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;

            case MEDTYPE_ALL:
                table = MedicineType._T;
                selection = null;
                selectionArgs = null;
                break;
            case MEDTYPE_SINGLE:
                table = MedicineType._T;
                selection = String.format("%s = ?", MedicineType.ID);
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        int count = db.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //--------------------------------------------------------------------------------------------------------------------

    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        ContentProviderResult[] result = new ContentProviderResult[operations.size()];
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int i = 0;
            for (ContentProviderOperation operation : operations) {
                result[i] = operation.apply(this, result, i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return result;
    }

    //--------------------------------------------------------------------------------------------------------------------

}
