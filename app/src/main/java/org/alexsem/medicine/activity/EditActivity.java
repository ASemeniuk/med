package org.alexsem.medicine.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.alexsem.medicine.R;
import org.alexsem.medicine.adapter.MedicineGroupAdapter;
import org.alexsem.medicine.adapter.MedicineMonthAdapter;
import org.alexsem.medicine.adapter.MedicineTypeAdapter;
import org.alexsem.medicine.adapter.MedicineYearAdapter;
import org.alexsem.medicine.transfer.MedicineProvider;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_MEDICINE_ID = "medicineId";
    public static final int RESULT_GROUP_ADDED = RESULT_FIRST_USER + 1;

    private Spinner mGroup;
    private Button mGroupAdd;
    private EditText mName;
    private Spinner mType;
    private EditText mAmount;
    private TextView mUnit;
    private Spinner mMonth;
    private Spinner mYear;
    private EditText mDescription;
    private EditText mLink;

    private MedicineGroupAdapter mGroupAdapter;
    private MedicineTypeAdapter mTypeAdapter;
    private MedicineMonthAdapter mMonthAdapter;
    private MedicineYearAdapter mYearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGroup = (Spinner) findViewById(R.id.edit_group);
        mGroupAdd = (Button) findViewById(R.id.edit_group_add);
        mName = (EditText) findViewById(R.id.edit_name);
        mType = (Spinner) findViewById(R.id.edit_type);
        mAmount = (EditText) findViewById(R.id.edit_amount);
        mUnit = (TextView) findViewById(R.id.edit_unit);
        mDescription = (EditText) findViewById(R.id.edit_description);
        mLink = (EditText) findViewById(R.id.edit_link);
        mMonth = (Spinner) findViewById(R.id.edit_month);
        mYear = (Spinner) findViewById(R.id.edit_year);

        mGroupAdd.setOnClickListener(mOnGroupAddClickListener);
        loadGroups(-1);
        loadTypes();
        loadExpiration();

        if (savedInstanceState != null) {
            long groupId = savedInstanceState.getLong("groupId");
            for (int i = 0; i < mGroupAdapter.getCount(); i++) {
                if (mGroupAdapter.getItemId(i) == groupId) {
                    mGroup.setSelection(i);
                    break;
                }
            }
            mName.setText(savedInstanceState.getString("name"));
            long typeId = savedInstanceState.getLong("typeId");
            for (int i = 0; i < mTypeAdapter.getCount(); i++) {
                if (mTypeAdapter.getItemId(i) == typeId) {
                    mType.setSelection(i);
                    break;
                }
            }
            mAmount.setText(savedInstanceState.getString("amount"));
            mUnit.setText(savedInstanceState.getString("unit"));
            mMonth.setSelection(savedInstanceState.getInt("month"));
            mYear.setSelection(savedInstanceState.getInt("year") - mYearAdapter.MIN_YEAR);
            mDescription.setText(savedInstanceState.getString("description"));
            mLink.setText(savedInstanceState.getString("link"));
        } else if (getIntent().hasExtra(EXTRA_MEDICINE_ID)) {
            getSupportLoaderManager().initLoader(0, getIntent().getExtras(), mLoaderCallbacks);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Load list of medicine groups
     * @param groupToSelect id of group to select, or -1 to not bother selecting
     */
    private void loadGroups(long groupToSelect) {
        Uri uri = MedicineProvider.MedicineGroup.CONTENT_URI;
        String[] projection = {
                MedicineProvider.MedicineGroup.ID,
                MedicineProvider.MedicineGroup.NAME
        };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        mGroupAdapter = new MedicineGroupAdapter(this, cursor);
        mGroup.setAdapter(mGroupAdapter);
        if (groupToSelect > -1) {
            for (int i = 0; i < mGroupAdapter.getCount(); i++) {
                if (mGroupAdapter.getItemId(i) == groupToSelect) {
                    mGroup.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Load and populate list of medicine types
     */
    private void loadTypes() {
        Uri uri = MedicineProvider.MedicineType.CONTENT_URI;
        String[] projection = {
                MedicineProvider.MedicineType.ID,
                MedicineProvider.MedicineType.TYPE,
                MedicineProvider.MedicineType.UNIT,
                MedicineProvider.MedicineType.MEASURABLE
        };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        mTypeAdapter = new MedicineTypeAdapter(this, cursor);
        mType.setAdapter(mTypeAdapter);
        mType.setOnItemSelectedListener(mOnTypeSelectedListener);
        mType.setSelection(0);
    }

    /**
     * Load and populate list of months and years
     */
    private void loadExpiration() {
        Calendar now = Calendar.getInstance();
        mMonthAdapter = new MedicineMonthAdapter(this);
        mMonth.setAdapter(mMonthAdapter);
        mMonth.setSelection(now.get(Calendar.MONTH));

        mYearAdapter = new MedicineYearAdapter(this);
        mYear.setAdapter(mYearAdapter);
        mYear.setSelection(now.get(Calendar.YEAR) - mYearAdapter.MIN_YEAR);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                if (mName.getText().toString().trim().length() == 0) {
                    mName.setError(getString(R.string.edit_error_name));
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MedicineProvider.Medicine.NAME, mName.getText().toString());
                    values.put(MedicineProvider.Medicine.GROUP_ID, mGroup.getSelectedItemId());
                    values.put(MedicineProvider.Medicine.DESCRIPTION, mDescription.getText().toString());
                    values.put(MedicineProvider.Medicine.LINK, mLink.getText().toString());
                    values.put(MedicineProvider.Medicine.TYPE_ID, mType.getSelectedItemId());
                    int amount;
                    try {
                        amount = Integer.valueOf(mAmount.getText().toString());
                    } catch (NumberFormatException ex) {
                        amount = 0;
                    }
                    values.put(MedicineProvider.Medicine.AMOUNT, amount);
                    values.put(MedicineProvider.Medicine.EXPIRATION, MedicineProvider.formatExpireDate(mMonth.getSelectedItemPosition() + 1, (Integer) mYear.getSelectedItem()));
                    if (getIntent().hasExtra(EXTRA_MEDICINE_ID)) { //Existing item
                        Uri uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_URI, String.valueOf(getIntent().getLongExtra(EXTRA_MEDICINE_ID, -1)));
                        getContentResolver().update(uri, values, null, null);
                    } else { //New item
                        getContentResolver().insert(MedicineProvider.Medicine.CONTENT_URI, values);
                    }
                    Toast.makeText(this, R.string.edit_success, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("groupId", mGroup.getSelectedItemId());
        outState.putString("name", mName.getText().toString());
        outState.putLong("typeId", mType.getSelectedItemId());
        outState.putString("amount", mAmount.getText().toString());
        outState.putString("unit", mUnit.getText().toString());
        outState.putInt("month", mMonth.getSelectedItemPosition());
        outState.putInt("year", (Integer) mYear.getSelectedItem());
        outState.putString("description", mDescription.getText().toString());
        outState.putString("link", mLink.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //----------------------------------------------------------------------------------------------

    private View.OnClickListener mOnGroupAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = getLayoutInflater().inflate(R.layout.dialog_group_add, null);
            final EditText input = (EditText) view.findViewById(R.id.group_name);
            new AlertDialog.Builder(EditActivity.this).setTitle(R.string.group_new).setView(view)
                    .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContentValues values = new ContentValues();
                            values.put(MedicineProvider.MedicineGroup.NAME, input.getText().toString());
                            Uri uri = getContentResolver().insert(MedicineProvider.MedicineGroup.CONTENT_URI, values);
                            if (uri != null) {
                                long id = Long.valueOf(uri.getLastPathSegment());
                                loadGroups(id);
                                setResult(RESULT_GROUP_ADDED);
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, null).show();
        }
    };

    private AdapterView.OnItemSelectedListener mOnTypeSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) mTypeAdapter.getItem(position);
            mUnit.setText(cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.UNIT)));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //----------------------------------------------------------------------------------------------

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_URI, String.valueOf(args.getLong(EXTRA_MEDICINE_ID)));
            String[] projection = {
                    MedicineProvider.Medicine.GROUP_ID,
                    MedicineProvider.Medicine.NAME,
                    MedicineProvider.Medicine.TYPE_ID,
                    MedicineProvider.Medicine.AMOUNT,
                    MedicineProvider.Medicine.EXPIRATION,
                    MedicineProvider.Medicine.DESCRIPTION,
                    MedicineProvider.Medicine.LINK
            };
            return new CursorLoader(EditActivity.this, uri, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            try {
                if (data.moveToFirst()) {
                    long groupId = data.getLong(data.getColumnIndex(MedicineProvider.Medicine.GROUP_ID));
                    for (int i = 0; i < mGroupAdapter.getCount(); i++) {
                        if (mGroupAdapter.getItemId(i) == groupId) {
                            mGroup.setSelection(i);
                            break;
                        }
                    }
                    mName.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.NAME)));
                    long typeId = data.getLong(data.getColumnIndex(MedicineProvider.Medicine.TYPE_ID));
                    for (int i = 0; i < mTypeAdapter.getCount(); i++) {
                        if (mTypeAdapter.getItemId(i) == typeId) {
                            mType.setSelection(i);
                            break;
                        }
                    }
                    mAmount.setText(String.valueOf(data.getInt(data.getColumnIndex(MedicineProvider.Medicine.AMOUNT))));
                    mOnTypeSelectedListener.onItemSelected(null, null, mType.getSelectedItemPosition(), typeId);
                    Date date;
                    try {
                        date = MedicineProvider.parseExpireDate(data.getString(data.getColumnIndex(MedicineProvider.Medicine.EXPIRATION)));
                    } catch (ParseException e) {
                        date = new Date();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    mMonth.setSelection(cal.get(Calendar.MONTH));
                    mYear.setSelection(cal.get(Calendar.YEAR) - mYearAdapter.MIN_YEAR);
                    mDescription.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.DESCRIPTION)));
                    mLink.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.DESCRIPTION)));
                }
            } finally {
                data.close();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
}
