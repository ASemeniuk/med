package org.alexsem.medicine.activity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.alexsem.medicine.R;
import org.alexsem.medicine.adapter.MedicineTypeAdapter;
import org.alexsem.medicine.transfer.MedicineProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_MEDICINE_ID = "medicineId";
    private static final SimpleDateFormat EXPIRATION_FORMAT = new SimpleDateFormat("MMM. yyyy", Locale.getDefault());

    private EditText mName;
    private Spinner mType;
    private EditText mAmount;
    private TextView mUnit;
    private TextView mExpiration;
    private EditText mDescription;

    private MedicineTypeAdapter mTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = (EditText) findViewById(R.id.edit_name);
        mType = (Spinner) findViewById(R.id.edit_type);
        mAmount = (EditText) findViewById(R.id.edit_amount);
        mUnit = (TextView) findViewById(R.id.edit_unit);
        mDescription = (EditText) findViewById(R.id.edit_description);
        mExpiration = (TextView) findViewById(R.id.edit_expiration);
        mExpiration.setBackgroundDrawable(mName.getBackground());
        mExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                try {
                    c.setTime(EXPIRATION_FORMAT.parse(mExpiration.getText().toString()));
                } catch (Exception ex) {
                    c.setTime(new Date());
                }
                DatePickerDialog dialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mExpiration.setText(EXPIRATION_FORMAT.format(c.getTime()));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

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

        if (savedInstanceState != null) {
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
            mExpiration.setText(savedInstanceState.getString("expiration"));
            mDescription.setText(savedInstanceState.getString("description"));
        } else if (getIntent().hasExtra(EXTRA_MEDICINE_ID)) {
            getSupportLoaderManager().initLoader(0, getIntent().getExtras(), mLoaderCallbacks);
        } else {
            mExpiration.setText(EXPIRATION_FORMAT.format(new Date()));
        }
    }

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
                    values.put(MedicineProvider.Medicine.DESCRIPTION, mDescription.getText().toString());
                    values.put(MedicineProvider.Medicine.TYPE_ID, mType.getSelectedItemId());
                    int amount;
                    try {
                        amount = Integer.valueOf(mAmount.getText().toString());
                    } catch (NumberFormatException ex) {
                        amount = 0;
                    }
                    values.put(MedicineProvider.Medicine.AMOUNT, amount);
                    Date date;
                    try {
                        date = EXPIRATION_FORMAT.parse(mExpiration.getText().toString());
                    } catch (ParseException e) {
                        date = new Date();
                    }
                    values.put(MedicineProvider.Medicine.EXPIRATION, MedicineProvider.formatExpireDate(date));
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
        outState.putString("name", mName.getText().toString());
        outState.putLong("typeId", mType.getSelectedItemId());
        outState.putString("amount", mAmount.getText().toString());
        outState.putString("unit", mUnit.getText().toString());
        outState.putString("expiration", mExpiration.getText().toString());
        outState.putString("description", mDescription.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //----------------------------------------------------------------------------------------------

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
                    MedicineProvider.Medicine.NAME,
                    MedicineProvider.Medicine.TYPE_ID,
                    MedicineProvider.Medicine.AMOUNT,
                    MedicineProvider.Medicine.EXPIRATION,
                    MedicineProvider.Medicine.DESCRIPTION
            };
            return new CursorLoader(EditActivity.this, uri, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            try {
                if (data.moveToFirst()) {
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
                    mExpiration.setText(EXPIRATION_FORMAT.format(date));
                    mDescription.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.DESCRIPTION)));
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
